'use strict';

const db = require('seraph')(require('config').get('database'));
const model = require('seraph-model');
const Ticket = require('./ticket');

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

User.compose(Ticket, 'tickets', 'bought');

module.exports = User;
