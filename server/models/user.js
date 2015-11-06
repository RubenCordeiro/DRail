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
User.setUniqueKey('username');

var CreditCard = model(db, 'credit_card');
CreditCard.schema = {
    number: { type: String, required: true },
    expireDate: { type: Date, required: true }
};
CreditCard.setUniqueKey('number');
User.compose(CreditCard, 'creditCards', 'owns');

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

User.compose(Ticket, 'tickets', 'bought');

module.exports = User;
