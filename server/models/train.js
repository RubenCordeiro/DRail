'use strict';

const db = require('seraph')(require('config').get('database')),
    Lazy = require('lazy.js');

const model = require('seraph-model');

var Train = model(db, 'train');
Train.schema = {
    name: {type: String, required: true},
    capacity: {type: Number, min: 0}
};
Train.setUniqueKey('name');
Train.customMethods = {
    filter: (departureStationId, arrivalStationId, departureDate, callback) => {
        db.query('MATCH path = (departureStation:station)-[trips:trip*]->(arrivalStation:station) WHERE ID(departureStation) = {departureStationId} AND ID(arrivalStation) = {arrivalStationId} AND ALL(r in relationships(path) WHERE r.departureDate >= {departureDate}) RETURN EXTRACT(r in relationships(path) | [r.departureDate, r.trainId]) as trains, length(path) as pathLength',
            {
                departureStationId: departureStationId,
                arrivalStationId: arrivalStationId,
                departureDate: departureDate
            }, (err, result) => {
                if (err) {
                    return callback(err, null);
                }


                if (!result || result.length === 0) {
                    return callback(null, []);
                }

                result = Lazy(result).first();
                var trains = result.trains,
                    pathLength = result.pathLength;

                var trainIdMap = new Map();
                trains = Lazy(trains).map(train => {
                    var currentIdCount = trainIdMap.get(train[1]);
                    trainIdMap.set(train[1], currentIdCount ? currentIdCount + 1 : 1);
                    return { departureDate: train[0], id: train[1] }
                })
                .filter(train => trainIdMap.get(train.id) == pathLength)
                .sortBy('departureDate', true);

                return callback(null, trains.toArray());
            });
    }
};
module.exports = Train;
