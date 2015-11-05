'use strict';

const db = require('seraph')(require('config').get('database'));
const model = require('seraph-model');

var Train = model(db, 'train');
Train.schema = {
    name: {type: String, required: true},
    capacity: {type: Number, min: 0}
};
Train.setUniqueKey('name');

module.exports = Train;