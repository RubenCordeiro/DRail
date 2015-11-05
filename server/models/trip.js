'use strict';

const Lazy = require('lazy.js');
const db = require('seraph')(require('config').get('database'));

module.exports = {

    save: (departureStationId, arrivalStationId, departureDate, arrivalDate, trainId, distance, callback) => {
        db.relate(departureStationId, 'trip', arrivalStationId, {
            departureTime: departureDate,
            arrivalTime: arrivalDate,
            trainId: trainId,
            distance: distance
        }, (err, relationship) => {

            if (err) return callback(err, null);

            return callback(null, relationship);
        });
    },

    find: (departureStationId, arrivalStationId, callback) => {
        db.query('MATCH path = (departureStation:station)-[:trip*]-(arrivalStation:station) WHERE ID(departureStation) = {departureStationId} AND ID(arrivalStation) = {arrivalStationId} RETURN EXTRACT( n in nodes(path) | n) as stations, EXTRACT(r in relationships(path) | r) as trips, path, length(path)', {
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
                                departureTime: trip.properties.departureTime,
                                arrivalTime: trip.properties.arrivalTime,
                                distance: trip.properties.distance,
                                tripId: trip.id
                            }
                        })
                        .toArray();
                });


            return callback(null, ret.toArray());
        });
    },

    findShortest: (departureStationId, arrivalStationId, callback) => {
        db.query('MATCH (from: station {id: {departureStationId}}), (to: station {id: {arrivalStationId}}), path = shortestPath((from)-[:connected*]-(to)) WITH REDUCE(dist = 0, rel in rels(path) | dist + rel.distance) AS distance, path RETURN path, distance', {
            departureStationId: departureStationId,
            arrivalStationId: arrivalStationId
        }, (err, trip) => {
            if (err) return callback(err, null);

            return callback(null, trip);
        });
    }
};
