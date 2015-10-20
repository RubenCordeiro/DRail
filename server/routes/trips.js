var Boom = require('boom');
var Joi = require('joi');
var Trip = require('../models/trip');

module.exports = function(server) {

    server.route({
        method: 'GET',
        path: '/api/trips',
        config: {
            validate: {
                query: {
                    from: Joi.number().integer().required(),
                    to: Joi.number().integer().required()
                }
            }
        },
        handler: function(request, reply) {
            Trip.find(request.query.from, request.query.to, function(err, trips) {
                if (err) return reply(Boom.badImplementation('Internal server error', err));

                return reply(trips);
            });
        }
    });

};