'use strict';

const Boom = require('boom');
const Joi = require('joi');

const Train = require('../models/train');

module.exports = function(server) {

    server.route({
        method: 'GET',
        path: '/api/trains',
        config: {
            auth: false,
            validate: {
                query: {
                    departureStation: Joi.number().integer(),
                    arrivalStation: Joi.number().integer(),
                    departureDate: Joi.date()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {

            var query = request.query;

            /* filter trains */
            if (typeof query.departureStation != 'undefined' && typeof query.arrivalStation != 'undefined' && query.departureDate) {
                Train.customMethods.filter(query.departureStation, query.arrivalStation, query.departureDate, (err, trains) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(trains);
                })
            } else {
                Train.findAll((err, trains) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(trains);
                });
            }
        }
    });

    server.route({
        method: 'GET',
        path: '/api/trains/{id}',
        config: {
            validate: {
                params: {
                    id: Joi.number().integer().required()
                }
            },
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {
            Train.read(request.params.id, (err, train) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                return reply(train);
            });
        }
    });

    server.route({
        method: 'POST',
        path: '/api/trains',
        config: {
            validate: {
                payload: {
                    name: Joi.string().required()
                }
            },
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {
            Train.where({ name: request.payload.name }, (err, trains) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badRequest('Internal server error'));
                }

                if (trains && trains.length > 0) {
                    return reply(Boom.badRequest('A train with the given name already exists'));
                }

                Train.save({ name: request.payload.name }, (err, newTrain) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(newTrain).code(201);
                });
            });
        }
    });
};
