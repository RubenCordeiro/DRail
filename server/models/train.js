var db = require('seraph')(require('config').get('database'));
var model = require('seraph-model');

var Train = model(db, 'train');
Train.schema = {
    name: {type: String, required: true}
};
Train.setUniqueKey('name');

module.exports = Train;