'use strict';

const Boom = require('boom');
const Joi = require('joi');
const Trip = require('../models/trip');

module.exports = function(server) {

    server.route({
        method: 'GET',
        path: '/api/trips',
        config: {
            auth: false,
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

    server.route({
        method: 'POST',
        path: '/api/trips',
        config: {
            auth: false,
            validate: {
                payload: {
                    from: Joi.number().integer().required(),
                    to: Joi.number().integer().required(),
                    distance: Joi.number().integer().required(),
                    departureDate: Joi.date().required(),
                    arrivalDate: Joi.date().required(),
                    trainId: Joi.number().integer().required()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            var payload = request.payload;

            Trip.save(payload.from, payload.to, payload.departureDate, payload.arrivalDate, payload.trainId, payload.distance,
                (err, trip) => {
                    if (err) return reply(Boom.badImplementation('Internal server error'));

                    return reply(trip);
            });
        }
    });

};
