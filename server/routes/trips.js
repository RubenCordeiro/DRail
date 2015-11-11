'use strict';

const Boom = require('boom'),
    Joi = require('joi'),
    Trip = require('../models/trip');

module.exports = function(server) {

    server.route({
        method: 'GET',
        path: '/api/trips/hydrated',
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

            Trip.findAndHydrate(request.query.from, request.query.to, (err, results) => {
                if (err) {
                    server.log(['error'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                return reply(results);
            });

        }
    });

    server.route({
        method: 'GET',
        path: '/api/trips',
        config: {
            auth: false,
            validate: {
                query: {
                    from: Joi.number().integer().required(),
                    to: Joi.number().integer().required(),
                    hydrate: Joi.boolean()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {

            if (request.query.hydrate) {
                Trip.findAndHydrate(request.query.from, request.query.to, (err, results) => {
                    if (err) {
                        server.log(['error'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(results);
                });
            } else {
                Trip.find(request.query.from, request.query.to, (err, results) => {
                    if (err) {
                        server.log(['error'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(results);
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
