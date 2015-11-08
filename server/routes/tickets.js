'use strict';

const Boom = require('boom'),
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
                    departureStation: Joi.number().integer(),
                    arrivalStation: Joi.number().integer(),
                    trainId: Joi.number().integer(),
                    departureDate: Joi.date()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            var query = request.query;

            if (query.departureStation && query.arrivalStation && query.trainId && query.departureDate) {
                Ticket.customMethods.filter(query.departureStation, query.arrivalStation, query.departureDate,
                    query.trainId, (err, tickets) => {
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
                    date: Joi.date().required(),
                    departureStation: Joi.number().integer().required(),
                    arrivalStation: Joi.number().integer().required()
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

                server.inject('/api/trips?from=' + request.payload.departureStation + '&to=' + request.payload.arrivalStation
                    + '&shortest=true',
                    (res) => {

                        if (!res.result || res.result.length === 0)
                            return reply(Boom.notFound('No valid trip was found'));

                        var trip = Lazy(res.result).first();

                        var reducer = (path, nextTrip) => {
                            if (nextTrip.start.isCentral) {
                                return path.concat([nextTrip]);
                            }
                            else {
                                path[path.length - 1].push(nextTrip);
                                return path;
                            }
                        };

                        var ticketPaths = trip.reduce(reducer, [[]]).filter((path) => path.length > 0)
                            .map((subpath) => subpath.map((segment) => segment.tripId));

                        var ticketDate = new Date();
                        Lazy(ticketPaths).async().each(subpath => {
                                User.push(user.id, 'tickets', { creationDate: ticketDate, trips: subpath, status: 'pending' }, (err) => {
                                    if (err) {
                                        server.log(['error', 'database'], err);
                                        throw err;
                                    }
                                });
                            })
                            .onComplete(() => reply('Ticket created successfully').code(201))
                            .onError(err => { // any exception is caught here
                                server.log(['error', 'database'], err);
                                reply(Boom.badImplementation('Internal server error'));
                            });
                    });
            });
        }
    });

};
