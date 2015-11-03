'use strict';

module.exports = (server) => {
    require('./users')(server);
    require('./stations')(server);
    require('./trips')(server);
    require('./trains')(server);

    // documentation route
    server.route({
        path: '/',
        method: 'GET',
        config: {
            auth: false
        },
        handler: (request, reply) => {
            reply.redirect('/docs')
        }
    });
};
