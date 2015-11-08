'use strict';

const Boom = require('boom');
const Joi = require('joi');
const Lazy = require('lazy.js');

const Station = require('../models/station');
const db = require('seraph')(require('config').get('database'));


module.exports = (server) => {
    server.route({
        method: 'GET',
        path: '/api/stations',
        config: {
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {
            Station.findAll( (err, stations) => {
                if (err)
                    reply(Boom.badImplementation('Internal server error', err));
                else
                    reply(stations);
            });
        }
    });

    server.route({
        method: 'GET',
        path: '/api/stations/{id}',
        config: {
            auth: false,
            validate: {
                params: {
                    id: Joi.number().integer()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            Station.where({id: request.params.id}, (err, station) => {
                if (err)
                    reply(Boom.badImplementation('Internal server error', err));
                else
                    reply(station);
            });
        }
    });

    server.route({
        method: 'POST',
        path: '/api/stations',
        config: {
            auth: false,
            validate: {
                payload: {
                    name: Joi.string().min(1).max(20).required(),
                    isCentral: Joi.boolean().required(),
                    latitude: Joi.number().required(),
                    longitude: Joi.number().required(),
                    trips: Joi.array().items(Joi.object().keys({
                        id: Joi.number().integer(),
                        departureTime: Joi.date(),
                        arrivalTime: Joi.date(),
                        trainId: Joi.number().integer(),
                        distance: Joi.number().integer()
                    }))
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            var txn = db.batch();

            var newStation = txn.save({
                name: request.payload.name,
                isCentral: request.payload.isCentral,
                latitude: request.payload.latitude,
                longitude: request.payload.longitude
            });
            txn.label(newStation, 'station');
            if (request.payload.trips) {
                request.payload.trips.forEach( (trip) => {
                    txn.relate(newStation, 'trip', trip.id, {
                        distance: trip.distance,
                        departureTime: trip.departureTime,
                        arrivalTime: trip.arrivalTime,
                        trainId: trip.trainId
                    });
                });
            }

            txn.commit( (err) => {
                if (err) return reply(Boom.badImplementation('Internal server error', err));

                return reply(newStation);
            });

        }
    });

    server.route({
        method: 'GET',
        path: '/api/stations/{id}/trips',
        config: {
            auth: false,
            validate: {
                params: {
                    id: Joi.number().integer().required()
                }
            },
            tags: ['api']
        },
        handler: (request, reply) => {
            db.query('MATCH (from:station)-[trips:trip]->(destination:station) WHERE ID(from) = {stationId} RETURN trips, destination',
                {
                    stationId: request.params.id
                },
                (err, trips) => {
                    if (err) return reply(Boom.badImplementation('Internal server error', err));


                    var formattedTrips = Lazy(trips).map((trip) => {
                        var ret = trip.trips.properties;
                        ret.id = trip.trips.id;
                        ret.destination = trip.destination;
                        return ret;
                    });

                    return reply(formattedTrips.toArray());
                });
        }
    });
};
