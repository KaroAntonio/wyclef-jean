
$(document).ready(function() {
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
		'strokes':[],
		'mode':'draw',
		'lat':43.657283,
		'lng':-79.395747
	}

	update_window(state);
	create_buttons(state);

	$(window).resize(function() {
		update_window(state);
	})

	map = init_map(state);


	load_strokes(state);
	update_strokes(state);

})

function create_buttons(state) {
	// ummm do this better, i mean jk this is untouchable

	var control_panel = $('<div>');
	var draw_button = $('<div>');
	var move_button = $('<div>');

	control_panel.attr('id','control-panel')
	draw_button.attr('id','draw-button')
	move_button.attr('id','move-button')

	control_panel.append(draw_button)
	control_panel.append(move_button)

	$('body').append(control_panel)
	
	control_panel.css({
		'position':'fixed',
		'left':20,
		'top':state.h/2 - 100
	})

	$('#move-button, #draw-button').css({
		'width':50,
		'height':50,
		'opacity':0.3,
		'color':'white',
		'background':'black',
		'text-align':'center'
	})
	draw_button.css({
		'opacity':1
	})

	move_button.text('move')
	draw_button.text('draw')

	move_button.click(function() {
		state.mode = 'move'	
		state.pointer = 'up'
		move_button.css({
			'opacity':1
		})
		draw_button.css({
			'opacity':0.3
		})
		map.setOptions({
			draggable: true, 
			zoomControl: true, 
			scrollwheel: true, 
			disableDoubleClickZoom: false, 
			clickableIcons: true
		});
	})

	draw_button.click(function() {
		state.mode = 'draw'	
		state.pointer = 'up'
		move_button.css({
			'opacity':0.3
		})
		draw_button.css({
			'opacity':1
		})
		map.setOptions({
			draggable: false, 
			zoomControl: false, 
			scrollwheel: false, 
			disableDoubleClickZoom: true, 
			clickableIcons: false
		});
	})
}

function update_window(state) {
	state['w'] = window.innerWidth;
	state['h'] = window.innerHeight

	$("#map-container").css({
		'margin':'20px',
		'width':state.w-40,
		'height':state.h-40
	})

}

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

	if (navigator.geolocation) {
		// geolocation is available
		navigator.geolocation.getCurrentPosition(
			geo_success,
			geo_error,
			{maximumAge:600000, timeout:10000})
		
		function geo_success(pos){
			state.lat = pos.coords.latitude
			state.lng = pos.coords.longitude
			state.map.setOptions({
				center:new google.maps.LatLng(state.lat, state.lng)
			})
		}

		function geo_error(err) {
			// TODO make a better error
			alert('GEO LOCATION ERROR');
			console.log(err);
		}
	} 
	else {
		console.log('NO GEOLOCATION')
		// geolocation is not supported
	}


	var mapOptions = {
		center:new google.maps.LatLng(state.lat, state.lng), zoom:15,
		mapTypeId:google.maps.MapTypeId.ROADMAP,
		styles: myStyles	
	};
		
	var map = new google.maps.Map(document.getElementById("map-container"),mapOptions);
	state['map'] = map;

	map.addListener('click',function(e) { console.log('click') } );
	map.addListener('mouseup',function(e) { finish_stroke(state) } );
	map.addListener('mousedown',function(e) { state['pointer'] = 'down'; update_strokes(state) } )
	map.addListener('mousemove',function(e) { build_stroke(state,e); update_strokes(state) } )

	map.setOptions({draggable: false, zoomControl: false, scrollwheel: false, disableDoubleClickZoom: true, clickableIcons: false});

	return map
}

function finish_stroke(state) {

	if (state.mode != 'draw') return;
	
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
	/*
	$.post(
		'url',
		curr_stroke
		).done(function(){
			console.log('ya dat stroke got thru');
		});*/
			
}

function build_stroke(state,e) {
	if (state.pointer == 'down' && state.mode == 'draw') {
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

