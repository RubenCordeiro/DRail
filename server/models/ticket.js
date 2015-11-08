'use strict';

const db = require('seraph')(require('config').get('database'));
const Sha1 = require('crypto-js/sha1');
const model = require('seraph-model');

var Ticket = model(db, 'ticket');
Ticket.schema = {
    creationDate: { type: Date, required: true },
    trips: { type: Array, required: true }, // array of trip ID's
    status: { type: String, required: true, default: 'pending'}
};
Ticket.addComputedField('signature', function(ticket) {
    return Sha1(ticket.id.toString() + ticket.creationDate).toString();
});

var Train = require('./train');
Ticket.compose(Train, 'train', 'belongsTo');

Ticket.customMethods = {
    filter: (departureStationId, arrivalStationId, departureDate, trainId, callback) => {
        db.query('MATCH path = (departureStation:station)-[trips:trip*]->(arrivalStation:station) ' +
            'WHERE ID(departureStation) = {departureStationId} AND ID(arrivalStation) = {arrivalStationId} AND ALL(r in relationships(path) ' +
            'WHERE r.departureDate >= {departureDate} AND r.trainId = {trainId}) WITH path MATCH (tickets:ticket) ' +
            'WHERE ANY(r in relationships(path) WHERE ID(r) IN tickets.trips) RETURN tickets',
            {
                departureStationId: departureStationId,
                arrivalStationId: arrivalStationId,
                departureDate: departureDate,
                trainId: trainId
            },
            (err, results) =>{
                if (err) {
                    return callback(err, null);
                }

                return callback(null, results);
            });
    }
};

module.exports = Ticket;
