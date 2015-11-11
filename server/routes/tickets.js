'use strict';

const Async = require('async'),
    Boom = require('boom'),
    Joi = require('joi'),
    Lazy = require('lazy.js');

const User = require('../models/user'),
    Ticket = require('../models/ticket');

const db = require('seraph')(require('config').get('database'));

module.exports = (server) => {

    server.route({
        method: 'GET',
        path: '/api/tickets',
        config: {
            auth: false,
            validate: {
                query: {
                    trips: Joi.array().items(Joi.number().integer())
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            var query = request.query;

            if (typeof query.trips !== 'undefined') {
                Ticket.customMethods.filter(query.trips,
                    (err, tickets) => {
                        if (err) {
                            server.log(['error', 'database'], err);
                            return reply(Boom.badImplementation('Internal server error'));
                        }

                        return reply(tickets);
                    });
            } else {
                Ticket.findAll((err, tickets) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    return reply(tickets);
                });
            }
        }

    });

    server.route({
        method: 'POST',
        path: '/api/tickets/validate',
        config: {
            auth: false,
            validate: {
                payload: {
                    tickets: Joi.array().items(Joi.object().keys({
                        id: Joi.string().required().description('Internal ticket ID'),
                        status: Joi.string().allow(['noShow', 'validated', 'pending']).required()
                            .description('New ticket status (noShow | validated | pending)')
                    })).required().description('Collection of tickets to be updated')
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            Lazy(request.payload.tickets)
                .async()
                .each(ticket => {
                    ticket.id = parseInt(ticket.id, 10);
                    db.query("MATCH (n:`ticket`) WHERE ID(n) = {id} SET n.status = {status} RETURN n",
                        ticket, (err, results) => {
                            if (err) {
                                console.log("Error:", err);
                                throw err;
                            }
                            
                            console.log(results);
                        });
                })
                .onComplete(() => {
                    return reply('The tickets were successfully updated');
                })
                .onError(err => {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                });
        }
    });

    server.route({
        method: 'POST',
        path: '/api/users/{userId}/tickets',
        config: {
            auth: false,
            validate: {
                params: {
                    userId: Joi.number().integer().required()
                },
                payload: {
                    trips: Joi.array().items(Joi.object().keys({
                        trainId: Joi.number().integer().required(),
                        id: Joi.number().integer().required()
                    })).required()
                }
            },
            tags: ['api', 'tickets']
        },
        handler: (request, reply) => {

            User.read(request.params.userId, (err, user) => {

                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                if (!user) {
                    return reply(Boom.notFound('User ID does not exist'));
                }

                var currentDate = new Date();
                var currentTrainId = Lazy(request.payload.trips).first().trainId;
                var requestedTrips = Lazy(request.payload.trips);

                Async.whilst(
                    () => requestedTrips.toArray().length != 0,
                    (callback) => {
                        var consecutiveTrips = requestedTrips.takeWhile((trip) => {
                            if (trip.trainId != currentTrainId) {
                                currentTrainId = trip.trainId;
                                return false;
                            } else
                                return true;
                        });

                        User.push(user.id, 'tickets', {
                            creationDate: currentDate,
                            trips: consecutiveTrips.pluck("id").toArray(),
                            status: 'pending'
                        }, (err) => {
                            if (err) {
                                return callback(err);
                            } else {
                                requestedTrips = requestedTrips.without(consecutiveTrips.toArray());
                                return callback(null);
                            }
                        });
                    },
                    (err) => {
                        if (err) {
                            server.log(['error', 'database'], err);
                            return reply(Boom.badImplementation('Internal server error'));
                        }

                        return reply('Tickets were created successfully');
                    }
                );
            });
        }
    });

};
