'use strict';

const Hapi = require('hapi');

const server = new Hapi.Server();
server.connection({
    port: 3000,
    labels: ['api']
});

require('./routes')(server);

const goodReporterOptions = {
    opsInterval: 1000,
    reporters: [{
        reporter: require('good-file'),
        events: {log: '*'},
        config: {path: './logs'}
    }]
};

server.register([
    require('inert'),
    require('vision'),
    {
        register: require('hapi-swaggered'),
        options: {
            tags: {
                'foobar/test': 'Example foobar description'
            },
            tagging: {
                pathLevel: 2
            },
            info: {
                title: 'DRail train network webservice',
                description: '',
                version: '1.0'
            },
            auth: false
        }
    },
    {
        register: require('hapi-swaggered-ui'),
        options: {
            title: 'DRail API',
            path: '/docs',
            swaggerOptions: {},
            auth: false
        }
    },
    {
        register: require('good'),
        options: goodReporterOptions
    }], {
    select: 'api'
}, (err) => {
    if (err) {
        throw err
    }

    server.start(() => {
        console.log('Server running at:', server.info.uri);
    });
});

