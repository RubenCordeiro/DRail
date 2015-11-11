'use strict';

const db = require('seraph')(require('config').get('database'));
const jsrsasign = require('jsrsasign');
const model = require('seraph-model');

const prvKeyPEM = "-----BEGIN RSA PRIVATE KEY-----\nMIIBywIBAAJhAMxmnGhwRwo84+v1NGUDXy+uoN14ckd9iKvmFC5Sb6V50JKQZ9rX\noosv3UBknKYh0KdotYcArbXjMjmpMrBlh0t8orkgr9h6egPKiSaf9IOuirV+mvPW\nWh4xVsNMdePP2QIDAQABAmAd/OPvkqFpiBtsT9I7C66YYUdqlrQ1dt5pUd0eGqwU\nm/WUuyjxe3d2cjREsT4mRYmDCt/A/YUX2pV/9YGuJvM6fgajAXDrMmK7PRomSWMh\nOhC2YZ40RCrmFhUMJnRfI50CMQDtiGnVlyjN5yD8MPqESKtmjiYWvf10jejiIlgS\na4ziyT7ho8SurKj9/iwBASepdfsCMQDcSsZU6M+2caksxoiJLVs67HyNuO/71q7a\n5FqhW0Qsl5sa6IiHLuEfv2yfiPthrTsCMQC6gikvyAAHJNt2ifK62eCTpzvrEYUo\n9qCMpxDmbcJy9DfGrnOp//K1dNUSeNiuq+8CMQC+jC9h4r9IoKglAtW3UnRHU6ep\nLv7mZ5x32m5KhBdEOx7+94Sg6fvw6jObC3Hl22ECMF7E0LHpiwbJxUgpHTcJrB0s\nCQQplNAB9mjSYgtb6fTWqL5IL27ClMTdXqqWZ8R1dw==\n-----END RSA PRIVATE KEY-----"

var Ticket = model(db, 'ticket');
Ticket.schema = {
    creationDate: { type: Date, required: true },
    trips: { type: Array, required: true }, // array of trip ID's
    status: { type: String, required: true, default: 'pending'}
};
Ticket.addComputedField('signature', function(ticket) {
    var sig = new jsrsasign.crypto.Signature({"alg": "SHA1withRSA"});
    sig.init(prvKeyPEM);
    sig.updateString(ticket.id.toString());
    return sig.sign();
});

var Train = require('./train');
Ticket.compose(Train, 'train', 'belongsTo');

Ticket.customMethods = {
    filter: (trips, callback) => {
        db.query('MATCH (tickets:ticket) WHERE ANY (trip in tickets.trips WHERE trip IN {trips}) RETURN DISTINCT(tickets)',
            {
                trips: trips
            },
            (err, results) =>{
                if (err) {
                    return callback(err, null);
                }

                return callback(null, results);
            });
    }
};

module.exports = Ticket;
