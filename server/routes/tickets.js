'use strict';

const Boom = require('boom'),
    Joi = require('joi'),
    Lazy = require('lazy.js');

const User = require('../models/user');

module.exports = (server) => {

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
                            User.push(user.id, 'tickets', { creationDate: ticketDate, trips: subpath }, (err) => {
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
