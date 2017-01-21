
$(document).ready(function() {
	firebase.auth().onAuthStateChanged(function(user) {
	  if (!user) {
	    document.location.href = '/login.html';
	  }
	});
	$('#loading').hide();
	
	// app state (global object)
	state = {
		'tags':['kanye was here'],
		'stroke_type':'solid',
		'user_id':0,
		'stroke_color':'#FF0000',
		'stroke_weight':2,
		'pointer':'up',
		'curr_stroke':[],
		'stroke_paths':[],
		'strokes':[]
	}

	map = init_map(state)
	
	// DISABLE DRAGGING
	map.setOptions({draggable: false, zoomControl: false, scrollwheel: false, disableDoubleClickZoom: true, clickableIcons: false});

	load_strokes(state);
	update_strokes(state);

})

function load_strokes(state) {
	// LOAD STROKES
	//state.stroke_paths.push();
}

function init_map(state) {
	var myStyles =[
		{
			featureType: "poi",
			elementType: "labels",
			stylers: [
				  { visibility: "off" }
			]
		}
	];

	var mapOptions = {
		center:new google.maps.LatLng(43.657283,-79.395747), zoom:15,
		mapTypeId:google.maps.MapTypeId.ROADMAP,
		styles: myStyles	
	};
		
	var map = new google.maps.Map(document.getElementById("sample"),mapOptions);
	state['map'] = map

	map.addListener('click',function(e) { console.log('click') } );
	map.addListener('mouseup',function(e) { finish_stroke(state) } )
	map.addListener('mousedown',function(e) { state['pointer'] = 'down'; update_strokes(state) } )
	map.addListener('mousemove',function(e) { build_stroke(state,e); update_strokes(state) } )

	return map
}

function finish_stroke(state) {
	// Finish building stroke
	var curr_stroke = state.curr_stroke;
	state.stroke_paths.push(state.curr_stroke);
	state.curr_stroke = []
	state['pointer'] = 'up';

	// Build stroke pkg
	var stroke_data = {
		'path_coords':curr_stroke,
		'stroke_type':state.stroke_type,
		'tags':state.tags,
		'author_id':state.user_id,
		'timestamp':Date.now(),
		'stroke_color':state.stroke_color,
		'stroke_weight':state.stroke_weight
	}
	
	// Post stroke	
	console.log(stroke_data);
	/*
	$.post(
		'url',
		curr_stroke
		).done(function(){
			console.log('ya dat stroke got thru');
		});*/
			
}

function build_stroke(state,e) {

	if (state.pointer == 'down') {
		state.curr_stroke.push({lat: e.latLng.lat(), lng: e.latLng.lng()})
	}
}

function update_strokes(state) {

	// REMOVE existing strokes from map
	while(state.strokes.length > 0) {
		state.strokes[0].setMap(null);
		state.strokes.splice(0,1);
	}

	// REDRAW all strokes
	state.stroke_paths.forEach(function(stroke) {
		draw_stroke(state,stroke)
	});
	draw_stroke(state,state.curr_stroke)
}

function draw_stroke(state, path_coords) {
	var path = new google.maps.Polyline({
		clickable:false,
		path: path_coords,
		geodesic: true,
		strokeColor: state.stroke_color,
		strokeOpacity: 1.0,
		strokeWeight: state.stroke_weight

	});

	state.strokes.push(path)	

	path.setMap(state.map);
}


