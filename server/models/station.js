'use strict';

const db = require('seraph')(require('config').get('database'));

var Station = require('seraph-model')(db, 'station');
Station.schema = {
    name: { type: String, required: true },
    isCentral: { type: Boolean, default: false },
    latitude: { type: Number, required: true },
    longitude: { type: Number, required: true }
};
Station.setUniqueKey('name');

module.exports = Station;
