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
    filter: (trips, callback) => {
        db.query('MATCH (tickets:ticket) WHERE ANY (trip in tickets.trips WHERE trip IN {trips}) RETURN DISTINCT(tickets)',
            {
                trips: trips
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
