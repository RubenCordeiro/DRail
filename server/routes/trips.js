'use strict';

const Boom = require('boom');
const Joi = require('joi');
const Trip = require('../models/trip');

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
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            Trip.find(request.query.from, request.query.to, function(err, trips) {
                if (err) return reply(err).code(500);

                return reply(trips);
            });
        }
    });

};