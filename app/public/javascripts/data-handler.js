var database = firebase.database();
var geoFire = new GeoFire(database.ref('stroke_geo'));

function saveStroke(stroke) {
    var user = firebase.auth().currentUser;
    if (user) {
        var strokesRef = database.ref('users/' + user.uid + '/strokes').push(stroke);

        var strokeId = strokesRef.key;
        var coords = stroke.path_coords.map(locToArr);

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
    geoQuery.on("ready", function() {
        console.log("GeoQuery has loaded and fired all other events for initial data");
    });
    var strokesRecieved = [];
    geoQuery.on("key_entered", function(key, location, distance) {
        var strokeId = key.substr(0, key.lastIndexOf('-'));
        if (!strokesRecieved.includes(strokeId)) {
            var user = firebase.auth().currentUser;
            var strokeRef = database.ref('users/' + user.uid + '/strokes/' + strokeId);
            strokeRef.on('value', function(snapshot){
                console.log(strokeId);
                listener(snapshot.val());
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