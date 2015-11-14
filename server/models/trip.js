'use strict';

const Lazy = require('lazy.js');
const Async = require('async');
const db = require('seraph')(require('config').get('database'));

module.exports = {

    save: (departureStationId, arrivalStationId, departureDate, arrivalDate, trainId, distance, callback) => {
        db.relate(departureStationId, 'trip', arrivalStationId, {
            departureDate: departureDate,
            arrivalDate: arrivalDate,
            trainId: trainId,
            distance: distance
        }, (err, relationship) => {

            if (err) return callback(err, null);

            return callback(null, relationship);
        });
    },

    findAndHydrate: (departureStationId, arrivalStationId, callback) => {

        db.query('START startNode=Node({departureStationId}), endNode=Node({arrivalStationId}) ' +
            'MATCH path=shortestPath(startNode-[trips:trip*]-endNode) ' +
            'RETURN EXTRACT(n in NODES(path) | {id: ID(n), name: n.name }) as stations',
            {
                departureStationId: departureStationId,
                arrivalStationId: arrivalStationId
            },
            (err, results) => {
                if (err) {
                    return callback(err, null);
                }

                var stations = Lazy(results[0]).pluck('id').toArray();
                if (stations.length < 2) {
                    return callback(null, []);
                }

                var first2Stations = Lazy(stations).first(2).toArray();

                db.query('MATCH path = (departureStation:station)-[trips:trip]->(arrivalStation:station) ' +
                    'WHERE ID(departureStation) = {departureStation} AND ID(arrivalStation) = {arrivalStation} RETURN EXTRACT(r IN relationships(path) ' +
                    '| {id: ID(r), departureDate: r.departureDate, arrivalDate: r.arrivalDate, trainId: r.trainId, prevStationName: STARTNODE(r).name, nextStationName: ENDNODE(r).name}) ' +
                    'ORDER BY trips.departureDate',
                    {
                        departureStation: first2Stations[0],
                        arrivalStation: first2Stations[1]
                    },
                    (err, results) => {
                        if (err) {
                            return callback(err, null);
                        }

                        results = Lazy(results).map((result) => result[0]).toArray();
                        var reducer = (lastElem, currentStation, callback) => {
                            db.query('MATCH path = (departureStation:station)-[trips:trip]->(arrivalStation:station) ' +
                                'WHERE ID(departureStation) = {departureStation} AND ID(arrivalStation) = {arrivalStation} AND trips.departureDate >= {lastArrivalDate} ' +
                                'RETURN EXTRACT(r IN relationships(path) | {id: ID(r), departureDate: r.departureDate, ' +
                                'arrivalDate: r.arrivalDate, trainId: r.trainId, prevStationName: STARTNODE(r).name, nextStationName: ENDNODE(r).name}) ' +
                                'ORDER BY trips.departureDate LIMIT 1',
                                {
                                    departureStation: lastElem.prevStation.id,
                                    arrivalStation: currentStation,
                                    lastArrivalDate: lastElem.prevStation.arrivalDate
                                },
                                (err, results) => {
                                    if (err) {
                                        console.log("Err:", err);
                                        return callback(err, null);
                                    }

                                    if (results.length == 0) {
                                        return callback('Invalid trip', null);
                                    }

                                    return callback(null, {prevStation: {id: currentStation, arrivalDate: results[0][0].arrivalDate}, trips: lastElem.trips.concat(results[0][0])});
                                });
                        };

                        var nextStations = Lazy(stations).skip(2).toArray();
                        stations = Lazy(stations).toArray();
                        var ret = [[]];
                        Async.forEachOf(results, (result, index, callback) => {
                                Async.reduce(nextStations,  {trips: [], prevStation: { id: stations[1], arrivalDate: results[index].arrivalDate } }, reducer,
                                    (err, path) => {
                                        if (!err) {
                                            path.trips.unshift(results[index]);
                                            ret[index] = path.trips;
                                            callback(null);
                                        }
                                    });
                            },
                            (err) => {
                                if (err) {
                                    return callback(err, null);
                                }

                                return callback(null, ret);
                            });
                    });
            });
    },

    find: (departureStationId, arrivalStationId, callback) => {

        db.query('START startNode=Node({departureStationId}), endNode=Node({arrivalStationId}) ' +
            'MATCH path=shortestPath(startNode-[trips:trip*]-endNode) ' +
            'RETURN EXTRACT(n in NODES(path) | ID(n)) as stations',
            {
                departureStationId: departureStationId,
                arrivalStationId: arrivalStationId
            },
            (err, results) => {
                if (err) {
                    return callback(err, null);
                }

                var stations = results[0];
                if (stations.length < 2) {
                    return callback(null, []);
                }

                var first2Stations = Lazy(stations).first(2).toArray();

                db.query('MATCH path = (departureStation:station)-[trips:trip]->(arrivalStation:station) ' +
                    'WHERE ID(departureStation) = {departureStation} AND ID(arrivalStation) = {arrivalStation} RETURN EXTRACT(r IN relationships(path) ' +
                    '| {id: ID(r), departureDate: r.departureDate, arrivalDate: r.arrivalDate, trainId: r.trainId}) ' +
                    'ORDER BY trips.departureDate',
                    {
                        departureStation: first2Stations[0],
                        arrivalStation: first2Stations[1]
                    },
                    (err, results) => {
                        if (err) {
                            return callback(err, null);
                        }

                        results = Lazy(results).map((result) => result[0]).toArray();
                        var reducer = (lastElem, currentStation, callback) => {

                            db.query('MATCH path = (departureStation:station)-[trips:trip]->(arrivalStation:station) ' +
                                'WHERE ID(departureStation) = {departureStation} AND ID(arrivalStation) = {arrivalStation} AND trips.departureDate >= {lastArrivalDate} ' +
                                'RETURN EXTRACT(r IN relationships(path) | {id: ID(r), departureDate: r.departureDate, ' +
                                'arrivalDate: r.arrivalDate, trainId: r.trainId}) ' +
                                'ORDER BY trips.departureDate LIMIT 1',
                                {
                                    departureStation: lastElem.prevStation.id,
                                    arrivalStation: currentStation,
                                    lastArrivalDate: lastElem.prevStation.arrivalDate
                                },
                                (err, results) => {
                                    if (err) {
                                        console.log("Err:", err);
                                        return callback(err, null);
                                    }

                                    if (results.length == 0) {
                                        return callback('Invalid trip', null);
                                    }

                                    return callback(null, {prevStation: {id: currentStation, arrivalDate: results[0][0].arrivalDate}, trips: lastElem.trips.concat(results[0][0])});
                                });
                        };

                        var nextStations = Lazy(stations).skip(2).toArray();
                        stations = Lazy(stations).toArray();
                        var ret = [[]];
                        Async.forEachOf(results, (result, index, callback) => {
                                Async.reduce(nextStations,  {trips: [], prevStation: { id: stations[1], arrivalDate: results[index].arrivalDate } }, reducer,
                                    (err, path) => {
                                        if (!err) {
                                            path.trips.unshift(results[index]);
                                            ret[index] = path.trips;
                                            callback(null);
                                        }
                                    });
                            },
                            (err) => {
                                if (err) {
                                    return callback(err, null);
                                }

                                return callback(null, ret);
                            });
                    });
            });
    },

    findShortest: (departureStationId, arrivalStationId, callback) => {
        db.query('MATCH (departureStation:station), (arrivalStation:station), path = shortestPath((departureStation)-[:trip*]-(arrivalStation)) WITH departureStation, arrivalStation, REDUCE(dist = 0, rel in rels(path) | dist + rel.distance) AS distance, path WHERE ID(departureStation) = {departureStationId} AND ID(arrivalStation) = {arrivalStationId} RETURN EXTRACT( n in nodes(path) | n) as stations, EXTRACT(r in relationships(path) | r) as trips, path, distance', {
            departureStationId: departureStationId,
            arrivalStationId: arrivalStationId
        }, (err, trips) => {
            if (err) return callback(err, null);

            var indexedSations = Lazy(trips)
                .map(function (elem) {
                    return elem.stations;
                })
                .flatten()
                .indexBy('id');

            var ret = Lazy(trips)
                .map(function (elem) {
                    return Lazy(elem.trips)
                        .map(function (trip) {
                            return {
                                start: indexedSations.get(trip.start),
                                end: indexedSations.get(trip.end),
                                trainId: trip.properties.trainId,
                                departureDate: trip.properties.departureDate,
                                arrivalDate: trip.properties.arrivalDate,
                                distance: trip.properties.distance,
                                tripId: trip.id
                            }
                        })
                        .toArray();
                })
                .filter((elem) => {

                    /* evaluates whether there are two consecutive trips that have incompatible timetables */
                    var evaluator = (trip1, trip2) => {

                        if (!trip1)
                            return null;

                        if (trip1.arrivalDate <= trip2.departureDate)
                            return trip2;


                        return null;

                    };
                    var validTrip = Lazy(elem).reduce(evaluator);

                    return validTrip !== null;
                });

            return callback(null, ret.toArray());
        });
    }
};
