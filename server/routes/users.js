'use strict';

const Boom = require('boom'),
    Joi = require('joi'),
    Jwt = require('jsonwebtoken'),
    Lazy = require('lazy.js');

const User = require('../models/user');

var validateToken = (decoded, request, callback) => {
    User.read(decoded.id, function (err, user) {
        if (err) return callback(err, false); // database error
        if (!user) return callback(null, false); // user not found

        return callback(null, true); // success
    });
};

module.exports = function (server) {

    server.register(require('hapi-auth-jwt2'), function (err) {
        if (err) return server.log(['error', 'authentication', 'jwt'], err);

        server.auth.strategy('jwt', 'jwt',
            {
                key: require('config').get('jwt'),
                validateFunc: validateToken,
                verifyOptions: {algorithms: ['HS256']}
            });

        server.auth.default('jwt');
    });

    server.route({
        method: 'POST',
        path: '/api/register',
        config: {
            validate: {
                payload: {
                    name: Joi.string().required(),
                    username: Joi.string().required(),
                    password: Joi.string().required(),
                    role: Joi.string().allow(['passenger', 'inspector']),
                    creditCards: Joi.array().items(Joi.object().keys({
                        expireDate: Joi.date().required(),
                        number: Joi.string().creditCard().required()
                    })).required()
                }
            },
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {

            User.exists({username: request.payload.username}, (err, userExists) => {

                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                if (userExists) return reply(Boom.badRequest('A user with the given username already exists'));

                User.save(request.payload, (err, newUser) => {
                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error', err));
                    }

                    return reply({
                        username: newUser.username,
                        id: newUser.id,
                        role: newUser.role,
                        token: Jwt.sign({
                            username: newUser.username,
                            id: newUser.id,
                            role: newUser.role
                        }, require('config').get('jwt'))
                    });
                });
            });

        }
    });

    server.route({
        method: 'POST',
        path: '/api/login',
        config: {
            validate: {
                payload: {
                    username: Joi.string().required(),
                    password: Joi.string().required()
                }
            },
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {
            User.where({username: request.payload.username, password: request.payload.password}, (err, users) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation("Internal server error"));
                }

                if (!users || users.length == 0)
                    return reply(Boom.unauthorized("No user found with the given username/password"));

                var user = users[0];
                return reply({
                    id: user.id,
                    username: user.username,
                    role: user.role,
                    token: Jwt.sign({
                        id: user.id,
                        username: user.username,
                        role: user.role
                    }, require('config').get('jwt'))
                });
            });
        }
    });

    server.route({
        method: 'GET',
        path: '/api/users',
        config: {
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {
            User.findAll((err, users) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                return reply(users.map((user) => {
                    user.password = undefined;
                    return user;
                }));
            });
        }
    });

    server.route({
        method: 'POST',
        path: '/api/users/{id}/creditCards',
        config: {
            auth: false,
            validate: {
                params: {
                    id: Joi.number().integer().required()
                },
                payload: {
                    number: Joi.string().creditCard().required(),
                    expireDate: Joi.date().required()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            User.push(request.params.id, 'creditCards', request.payload, (err, creditCards) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(err);
                }

                return reply(creditCards);
            });
        }
    });

    server.route({
        method: 'GET',
        path: '/api/users/{id}/tickets',
        config: {
            auth: 'jwt',
            validate: {
                params: {
                    id: Joi.number().integer().required()
                }
            }
        },
        handler: (request, reply) => {
            User.read(request.params.id, (err, user) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                if (!user)
                    return reply(Boom.notFound("User does not exist"));

                return reply(user.tickets);
            });
        }
    });

    server.route({
        method: 'GET',
        path: '/api/users/{userId}/tickets/{ticketId}',
        config: {
            auth: 'jwt',
            validate: {
                params: {
                    userId: Joi.number().integer().required(),
                    ticketId: Joi.number().integer().required()
                }
            }
        },
        handler: (request, reply) => {
            User.read(request.params.userId, (err, user) => {
                if (err) {
                    server.log(['error', 'database'], err);
                    return reply(Boom.badImplementation('Internal server error'));
                }

                if (!user)
                    return reply(Boom.notFound('User not found'));

                var ticket = Lazy(user.tickets).find((ticket) => { return ticket.id == request.params.ticketId; });

                if (!ticket)
                    return reply(Boom.badRequest("User does not own a ticket with the given id"));

                return reply(ticket);
            });
        }
    });

};
