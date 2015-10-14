var db = require('seraph')(require('config').get('database'));

var Station = require('seraph-model')(db, 'station');
Station.schema = {
    name: { type: String, required: true }
};
Station.setUniqueKey('name');

module.exports = Station;
