var Boom = require('boom');
var Station = require('../models/station');

module.exports = function (server) {
    server.route({
        method: 'GET',
        path: '/api/stations',
        handler: function (request, reply) {
            Station.findAll(function (err, stations) {
                if (err)
                    reply(Boom.badImplementation('Internal server error', err));
                else
                    reply(stations);
            });
        }
    });

    server.route({
        method: 'GET',
        path: '/api/stations/{name}',
        handler: function(request, reply){
            Station.where({ name: request.params.name }, function(err, station) {
                if (err)
                    reply(Boom.badImplementation('Internal server error', err));
                else
                    reply(station);
            });
        }
    });
};
