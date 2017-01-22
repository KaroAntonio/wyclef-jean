let database = firebase.database();
let geoFire = new GeoFire(database.ref('stroke_geo'));

function saveStroke(stroke) {
    let user = firebase.auth().currentUser;
    if (user) {
        var strokesRef = database.ref('users/' + user.uid + '/strokes').push(stroke);

        var strokeId = strokesRef.key;
        var coords = stroke.path_coords.map(locToArr);
        console.log()

        var geoFireEntry = {};
        for (var i = 0; i < coords.length; i++) {
            geoFireEntry[strokeId + '-' + i] = coords[i];
        }
        geoFire.set(geoFireEntry);
    } else {
        document.location.href = '/login.html';
    }
}

function getStrokes(loc, radius) {
    var geoQuery = geoFire.query({
        center: locToArr(loc),
        radius: radius
    });
    geoQuery.on("ready", function() {
        console.log("GeoQuery has loaded and fired all other events for initial data");
    });
    geoQuery.on("key_entered", function(key, location, distance) {
        console.log(key + " entered query at " + location + " (" + distance + " km from center)");
    });
}

let locToArr = function(obj) {
    return [obj.lat, obj.lng];
}

let arrToLoc = function(arr) {
    return {
        lat: arr[0],
        lng: arr[1]
    };
}