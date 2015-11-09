'use strict';

const Async = require('async'),
    Boom = require('boom'),
    Joi = require('joi'),
    Lazy = require('lazy.js');

const User = require('../models/user'),
    Ticket = require('../models/ticket');

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
                    Ticket.read(ticket.id, (err, ticketInstance) => {

                        if (err) {
                            throw err;
                        }

                        if (!ticketInstance) {
                            server.log(['error', 'ticket_validation'],
                                { message: 'Ticket with ID: ' + ticket.id + ' was not found in the database'});

                        } else {
                            ticketInstance.status = ticket.status;
                            Ticket.save(ticketInstance, (err) => {
                                if (err) {
                                    throw err;
                                }
                            });
                        }

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
                    () => requestedTrips.length() != 0,
                    (callback) => {
                        var consecutiveTrips = requestedTrips.takeWhile((trip) => {
                            if (trip.trainId != currentTrainId) {
                                currentTrainId = trip.trainId;
                                return false;
                            } else
                                return true;
                        }).toArray();

                        User.push(user.id, 'tickets', {
                            creationDate: currentDate,
                            trips: consecutiveTrips,
                            status: 'pending'
                        }, (err) => {
                            if (err) {
                                return callback(err);
                            } else {
                                return callback(null);
                            }
                        });

                        requestedTrips = requestedTrips.without(consecutiveTrips);
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
