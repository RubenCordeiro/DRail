var trip = {
    "from": 0,
    "to": 1,
    "distance": 10,
    "departureDate": "2015-11-10T00:00:00.000Z",
    "arrivalDate": "2015-11-10T00:42:00.000Z",
    "trainId": 8
};

var request = require('request');

function postTrip(t) {
    request({
        url: 'http://localhost:3000/api/trips',
        method: "POST",
        json: true,
        headers: {
            "content-type": "application/json"
        },
        body: t
    });
}

function postStation(s) {
    request({
        url: 'http://localhost:3000/api/stations',
        method: "POST",
        json: true,
        headers: {
            "content-type": "application/json"
        },
        body: s
    });
}

function postTrain(t) {
    request({
        url: 'http://localhost:3000/api/trains',
        method: "POST",
        json: true,
        headers: {
            "content-type": "application/json"
        },
        body: t
    });
}

postStation({
    "name": "A",
    "isCentral": false,
    "latitude": 0,
    "longitude": 0
});

postStation({
    "name": "A1",
    "isCentral": false,
    "latitude": 0,
    "longitude": 0
});

postStation({
    "name": "Central",
    "isCentral": true,
    "latitude": 0,
    "longitude": 0
});

postStation({
    "name": "B1",
    "isCentral": false,
    "latitude": 0,
    "longitude": 0
});

postStation({
    "name": "B",
    "isCentral": false,
    "latitude": 0,
    "longitude": 0
});

postStation({
    "name": "C1",
    "isCentral": false,
    "latitude": 0,
    "longitude": 0
});

postStation({
    "name": "C",
    "isCentral": false,
    "latitude": 0,
    "longitude": 0
});

postTrain({
    "name": "Red"
});

postTrain({
    "name": "Blue"
});

postTrain({
    "name": "Green"
});

var year = 2015;
var month = 10;
var day = 11;

var diff = 42;

var d = new Date(year, month, day, 0, 0, 0);
trip.trainId = 7;

for (var c = 0; c < 4; ++c) {
    for (var i = 0; i < 4; ++i) {
        trip.from = i;
        trip.to = i + 1;
        trip.departureDate = d.toISOString().slice(11, 19);
        d = (new Date(d.getTime() + diff * 60000));
        trip.arrivalDate = d.toISOString().slice(11, 19);
        console.log(trip);
        postTrip(trip);

    }

    for (i = 4; i > 0; --i) {
        trip.from = i;
        trip.to = i - 1;
        trip.departureDate = d.toISOString().slice(11, 19);
        d = (new Date(d.getTime() + diff * 60000));
        trip.arrivalDate = d.toISOString().slice(11, 19);
        console.log(trip);
        postTrip(trip);
    }
}

trip.trainId = 8;
d = new Date(year, month, day, 0, 0, 0);

for (c = 0; c < 4; ++c) {
    for (i = 4; i > 0; --i) {
        trip.from = i;
        trip.to = i - 1;
        trip.departureDate = d.toISOString().slice(11, 19);
        d = (new Date(d.getTime() + diff * 60000));
        trip.arrivalDate = d.toISOString().slice(11, 19);
        console.log(trip);
        postTrip(trip);

    }

    for (i = 0; i < 4; ++i) {
        trip.from = i;
        trip.to = i + 1;
        trip.departureDate = d.toISOString().slice(11, 19);
        d = (new Date(d.getTime() + diff * 60000));
        trip.arrivalDate = d.toISOString().slice(11, 19);
        console.log(trip);
        postTrip(trip);
    }
}

trip.trainId = 9;
d = new Date(year, month, day, 0, 0, 0);
diff = 20;

for (c = 0; c < 18; ++c) {
    for (i = 6; i > 4; --i) {
        if (i == 5) {
            trip.to = 2;
        } else {
            trip.to = i - 1;
        }
        trip.from = i;
        trip.departureDate = d.toISOString().slice(11, 19);
        d = (new Date(d.getTime() + diff * 60000));
        trip.arrivalDate = d.toISOString().slice(11, 19);
        console.log(trip);
        postTrip(trip);

    }

    for (i = 4; i < 6; ++i) {
        if (i == 4) {
            trip.from = 2;
        } else {
            trip.from = i;
        }
        trip.to = i + 1;
        trip.departureDate = d.toISOString().slice(11, 19);
        d = (new Date(d.getTime() + diff * 60000));
        trip.arrivalDate = d.toISOString().slice(11, 19);
        console.log(trip);
        postTrip(trip);
    }
}
