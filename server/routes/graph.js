'use strict';

const Boom = require('boom');
const db = require('seraph')(require('config').get('database'));
const Lazy = require('lazy.js');

module.exports = (server) => {

    server.route({
        method: 'GET',
        path: '/api/graph',
        config: {
            auth: false,
            tags: ['api']
        },
        handler: (request, reply) => {
            db.query('MATCH (allStations:station), (stations:station)-[trips:trip]->() WITH DISTINCT(trips) as newTrips,' +
                ' allStations return COLLECT(allStations) as stations, COLLECT(newTrips) as trips',
                {},
                (err, results) => {

                    if (err) {
                        server.log(['error', 'database'], err);
                        return reply(Boom.badImplementation('Internal server error'));
                    }

                    results = results[0];
                    var stations = results.stations,
                        trips = results.trips;

                    trips = Lazy(trips)
                        .uniq('id')
                        .map((trip) => {
                            return { start: trip.start, end: trip.end };
                        })
                        .toArray();

                    stations = Lazy(stations).uniq('id').toArray();

                    return reply({ stations: stations, trips: trips});
                });
        }
    });

};
