var Hapi = require('hapi');

var server = new Hapi.Server();
server.connection({
    port: 3000,
    labels: ['api']
});

require('./routes')(server);

var goodReporterOptions = {
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
            info: {
                title: 'Example API',
                description: 'Powered by node, hapi, joi, hapi-swaggered, hapi-swaggered-ui and swagger-ui',
                version: '1.0'
            }
        }
    },
    {
        register: require('hapi-swaggered-ui'),
        options: {
            title: 'Example API',
            path: '/docs',
            swaggerOptions: {} // see above
        }
    },
    {
        register: require('good'),
        options: goodReporterOptions
    }], {
    select: 'api'
}, function (err) {
    if (err) {
        throw err
    }

    server.start(function () {
        console.log('Server running at:', server.info.uri);
    });
});

