var db = require('seraph')(require('config').get('database'));

module.exports = {

    save: function (departureStationId, arrivalStationId, departureTime, arrivalTime, trainId, callback) {
        db.relate(departureStationId, 'trip', arrivalStationId, {
            departureTime: departureTime,
            arrivalTime: arrivalTime,
            traindId: trainId
        }, function (err, relationship) {

            if (err) return callback(err, null);

            return callback(null, relationship);
        });
    },

    find: function (departureStationId, arrivalStationId, callback) {
        db.query('MATCH path = (departureStation:station)-[:trip*]-(arrivalStation:station) WHERE ID(departureStation) = {departureStationId} AND ID(arrivalStation) = {arrivalStationId} RETURN EXTRACT( n in nodes(path) | n) as nodes_in_path, EXTRACT(r in relationships(path) | r), path, length(path)', {
            departureStationId: departureStationId,
            arrivalStationId: arrivalStationId
        }, function (err, trips) {
            if (err) return callback(err, null);

            return callback(null, trips);
        });
    },

    findShortest: function (departureStationId, arrivalStationId, callback) {
        db.query('MATCH (from: station {id: {departureStationId}}), (to: station {id: {arrivalStationId}}), path = shortestPath((from)-[:connected*]-(to)) WITH REDUCE(dist = 0, rel in rels(path) | dist + rel.distance) AS distance, path RETURN path, distance', {
            departureStationId: departureStationId,
            arrivalStationId: arrivalStationId
        }, function (err, trip) {
            if (err) return callback(err, null);

            return callback(null, trip);
        });
    }
};
