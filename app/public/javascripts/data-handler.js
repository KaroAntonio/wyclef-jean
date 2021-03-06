var database = firebase.database();
var geoFire = new GeoFire(database.ref('stroke_geo'));

function saveStroke(stroke) {
    var user = firebase.auth().currentUser;
    if (user) {
        var strokesRef = database.ref('strokes').push(stroke);

        var strokeId = strokesRef.key;
        var coords = stroke.path.map(locToArr);

        var geoFireEntry = {};
        for (var i = 0; i < coords.length; i++) {
            geoFireEntry[strokeId + '-' + i] = coords[i];
        }
        geoFire.set(geoFireEntry);

        return strokeId;
    } else {
        document.location.href = '/login.html';
    }
}

var geoQuery;
function subscribeToStrokes(loc, listener) {
    if (geoQuery) {
        geoQuery.cancel();
    }

    geoQuery = geoFire.query({
        center: locToArr(loc),
        radius: loc.rad * (40076 / 360)
    });

    var strokesRecieved = [];
    geoQuery.on("key_entered", function(key, location, distance) {
        var strokeId = key.substr(0, key.lastIndexOf('-'));
        if (!strokesRecieved.includes(strokeId)) {
            strokesRecieved.push(strokeId);
            var strokeRef = database.ref('strokes/' + strokeId);
            strokeRef.on('value', function(snapshot){
                var value = snapshot.val();
                value.id = strokeId;
                listener(value);
            });
        }
    });
}

var locToArr = function(obj) {
    return [obj.lat, obj.lng];
}

var arrToLoc = function(arr) {
    return {
        lat: arr[0],
        lng: arr[1]
    };
}
