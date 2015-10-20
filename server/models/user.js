var db = require('seraph')(require('config').get('database'));
var Sha1 = require('crypto-js/sha1');
var model = require('seraph-model');

var User = model(db, 'user');
User.schema = {
    name: { type: String, required: true },
    username : { type: String, required: true },
    password: { type: String, required: true }
};

var CreditCard = model(db, 'credit_card');
User.compose(CreditCard, 'creditCards', 'owns');

var Ticket = model(db, 'ticket');
Ticket.schema = {
    id: { type: String, required: true },
    date: { type: Date, required: true }
};
Ticket.addComputedField('signature', function(ticket) {
    return Sha1(ticket.id.toString() + ticket.date.getTime().toString());
});

var Train = require('./train');
Ticket.compose(Train, 'train', 'belongsTo');

var Station = require('./station');
Ticket.compose(Station, 'departureStation', 'departsFrom');
Ticket.compose(Station, 'arrivalStation', 'arrivesTo');

User.compose(Ticket, 'tickets', 'bought');


module.exports = User;
