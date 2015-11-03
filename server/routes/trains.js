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
            tags: ['api']
        },
        handler: (request, reply) => {
            Train.findAll((err, trains) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                return reply(trains);
            });
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
