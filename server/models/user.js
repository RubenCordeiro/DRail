var db = require('seraph')(require('config').get('database'));
var model = require('seraph-model');

var User = model(db, 'user');
User.schema = {
    name: { type: String, required: true },
    username : { type: String, required: true },
    password: { type: String, required: true }
};

var CreditCard = model(db, 'credit_card');
User.compose(CreditCard, 'creditCards', 'owns');

module.exports = User;
