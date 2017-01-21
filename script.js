
$(document).ready(function() {
	$('#loading').hide();
	map = init_map()
	
	// DISABLE DRAGGING
	map.setOptions({draggable: false, zoomControl: false, scrollwheel: false, disableDoubleClickZoom: true});

	// LOAD STROKES
	var flightPlanCoordinates = [
		{lat: 43.657283, lng: -79.395747},
		{lat: 43.658283, lng: -79.395747}
	];

	add_stroke(map)
	
})

function init_map() {
	var mapOptions = {
	   center:new google.maps.LatLng(43.657283,-79.395747), zoom:15,
	   mapTypeId:google.maps.MapTypeId.ROADMAP
	};
		
	var map = new google.maps.Map(document.getElementById("sample"),mapOptions);

	map.addListener('click',function(e) { console.log(e) } )
	return map
}

function add_stroke(map, path_coords) {
	var path = new google.maps.Polyline({
		path: path_coords,
		geodesic: true,
		strokeColor: '#FF0000',
		strokeOpacity: 1.0,
		strokeWeight: 2
	});

	flightPath.setMap(map);
}


