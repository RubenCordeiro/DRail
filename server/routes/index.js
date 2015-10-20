module.exports = function(server) {
    require('./stations')(server);
    require('./trips')(server);
};
