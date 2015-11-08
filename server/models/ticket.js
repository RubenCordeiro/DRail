'use strict';

const db = require('seraph')(require('config').get('database'));
const Sha1 = require('crypto-js/sha1');
const model = require('seraph-model');

var Ticket = model(db, 'ticket');
Ticket.schema = {
    creationDate: { type: Date, required: true },
    trips: { type: Array, required: true } // array of trip ID's
};
Ticket.addComputedField('signature', function(ticket) {
    return Sha1(ticket.id.toString() + ticket.creationDate).toString();
});

var Train = require('./train');
Ticket.compose(Train, 'train', 'belongsTo');