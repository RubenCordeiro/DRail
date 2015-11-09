'use strict';

const Boom = require('boom'),
    Joi = require('joi'),
    Trip = require('../models/trip');

module.exports = function(server) {

    server.route({
        method: 'GET',
        path: '/api/trips',
        config: {
            auth: false,
            validate: {
                query: {
                    from: Joi.number().integer().required(),
                    to: Joi.number().integer().required(),
                    shortest: Joi.boolean()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {

            if (!request.query.shortest) {
                Trip.find(request.query.from, request.query.to, (err, trips) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(trips);
                });
            } else {
                Trip.findShortest(request.query.from, request.query.to, (err, trips) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(trips);
                });
            }
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
                    departureDate: Joi.date().format('HH:mm:ss').raw().required(),
                    arrivalDate: Joi.date().format('HH:mm:ss').raw().required(),
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
