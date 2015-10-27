module.exports = function (server) {
    require('./users')(server);
    require('./stations')(server);
    require('./trips')(server);

    // documentation route
    server.route({
        path: '/',
        method: 'GET',
        config: {
            auth: false
        },
        handler: function (request, reply) {
            reply.redirect('/docs')
        }
    });
};
