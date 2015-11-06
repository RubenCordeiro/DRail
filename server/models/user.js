'use strict';

const db = require('seraph')(require('config').get('database'));
const Sha1 = require('crypto-js/sha1');
const model = require('seraph-model');

var User = model(db, 'user');
User.schema = {
    name: { type: String, required: true },
    username : { type: String, required: true },
    password: { type: String, required: true },
    role: { type: String, required: true }
};

var CreditCard = model(db, 'credit_card');
CreditCard.schema = {
    number: { type: String, required: true },
    expireDate: { type: Date, required: true }
};
User.compose(CreditCard, 'creditCards', 'owns');

var Ticket = model(db, 'ticket');
Ticket.schema = {
    id: { type: String, required: true },
    date: { type: Date, required: true },
    trips: { type: Array, required: true } // array of trip ID's
};
Ticket.addComputedField('signature', function(ticket) {
    return Sha1(ticket.id.toString() + ticket.date.getTime().toString());
});

var Train = require('./train');
Ticket.compose(Train, 'train', 'belongsTo');

User.compose(Ticket, 'tickets', 'bought');

module.exports = User;
