module.exports = function(server) {

    server.route({
        path: '/',
        method: 'GET',
        handler: function (request, reply) {
            reply.redirect('/docs')
        }
    });

    require('./stations')(server);
    require('./trips')(server);
};
