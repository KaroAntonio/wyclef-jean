var state;

$(document).ready(function() {
	firebase.auth().onAuthStateChanged(function(user) {
	  if (user) {
  		$('#loading').hide();

		// app state (global object)
		state = {
			'initialized':false,
			'update_locked':false,
			'tags':['kanye was here'],
			'stroke_type':'solid',
			'user_id': user.uid,
			'stroke_color':'#000000',
			'stroke_weight':2,
			'pointer':'up',
			'stroke_paths':{},  // data for strokes
			'strokes':{},  // references to gmaps strokes
			'mode':'draw',
			'lat':40.657283,
			'lng':-79.395747,
			'rad':0.002,
			'd_buff':0.0005  // the tolerance on how much you have to move before a redraw is triggered
		}

		update_window(state);

		map = init_map(state);
		build_buttons(state);
		state['curr_stroke'] = init_stroke(state)
		update_strokes(state);
		build_slider(state)

		$(window).resize(function() {
			update_window(state);
		})

	  } else {
	    document.location.href = '/login.html';
	  }
	});
})

function extract_tag(state) {
	// extract the tag from the input 
	var tag = $('#tag-input').val().trim()
	if (tag.length == 0) {
		tag = 'none'
	}
	return tag
}

function build_slider(state) {
	
	$('.nstSlider').nstSlider({
		"left_grip_selector": ".leftGrip",
		"right_grip_selector": ".rightGrip",
		"value_bar_selector": ".bar",
		"value_changed_callback": function(cause, leftValue, rightValue) {
					var $container = $(this).parent();
					$container.find('.leftLabel').text(leftValue);
					$container.find('.rightLabel').text(rightValue);
				},
		"highlight": {
			"grip_class": "gripHighlighted",
			"panel_selector": ".highlightPanel"
		}
	});
	$('.nstSlider').css({
		'position':'fixed',
	})

	$('#highlightRangeButton').click(function() {
			var highlightMin = Math.random() * 20,
				highlightMax = highlightMin + Math.random() * 80;
		$('.nstSlider').nstSlider('highlight_range', highlightMin, highlightMax);
	});
}

function init_stroke(state) {
	return {
		'user_id':state.user_id,
		'path':[],
		'tags':[extract_tag(state)],
		'color':state.stroke_color,
		'weight':state.stroke_weight,
		'type':state.stroke_type
	}
}

function build_buttons(state) {
	// ummm do this better, i mean jk this is untouchable

	panel = $('#control-panel')
	if (panel.length != 0) 
	{
		$('#picker').appendTo('body')
		panel.remove()
	}

	var control_panel = $('<div>');
	control_panel.attr('id','control-panel')
	$('body').append(control_panel)

	control_panel.css({
		cursor:'pointer',
		'position':'fixed',
		'left':20,
		'top':state.h/2 - 100
	})

	var button_infos = [
		['draw',draw_click],
		['move',move_click],
		['color',null],
		['tag',null],
		['center',center_click]
	]
	
	button_infos.forEach(function(info) {
		var button = $('<div>')
		button.attr('id',info[0]+'-button')
		control_panel.append(button)
		button.css({
			'width':50,
			'height':50,
			'opacity':0.3,
			'color':'white',
			'background':'black',
			'text-align':'center'
		})
		button.text(info[0])
		button.click(info[1])
		
	})
	
	$('#draw-button').css({
		'opacity':1
	})

	var picker = $('#picker')
	picker.css({'width':42})
	picker.appendTo($('#color-button'))

	function center_click() {
		state.map.setOptions({
			center:new google.maps.LatLng(state.lat, state.lng)
		})
	}

	var tag_input = $('<input>')
	tag_input.attr({'id':'tag-input'})
	$('#tag-button').append(tag_input)
	tag_input.css({
		width:42,
	})

	function move_click() {
		state.mode = 'move'	
		state.pointer = 'up'
		$('#move-button').css({
			'opacity':1
		})
		$('#draw-button').css({
			'opacity':0.3
		})
		map.setOptions({
			draggable: true, 
			zoomControl: true, 
			scrollwheel: true, 
			disableDoubleClickZoom: false, 
			clickableIcons: true
		});
	}

	function draw_click() {
		state.mode = 'draw'	
		state.pointer = 'up'
		$('#move-button').css({
			'opacity':0.3
		})
		$('#draw-button').css({
			'opacity':1
		})
		map.setOptions({
			draggable: false, 
			zoomControl: false, 
			scrollwheel: false, 
			disableDoubleClickZoom: true, 
			clickableIcons: false
		});
	}
}

function update_color(jscolor) {
	state.stroke_color = '#' + jscolor
	state.curr_stroke.color = state.stroke_color
}


function update_window(state) {
	state['w'] = window.innerWidth;
	state['h'] = window.innerHeight

	$("#map-container").css({
		'margin':'20px',
		'width':state.w-40,
		'height':state.h-40
	})
	
	build_buttons(state)
}

function add_stroke(stroke) {
	if (stroke) {
		if (!(stroke.id in state.stroke_paths)){
			state.stroke_paths[stroke.id] = stroke
			update_strokes(state);
		}
	} else {
		console.log('received stroke is null!')
	}
}

function update_geolocation(state, center_on_success) {
	if (!state.update_locked) {
		if (navigator.geolocation) {
			// geolocation is available
			navigator.geolocation.getCurrentPosition(
				geo_success,
				geo_error,
				{maximumAge:600000, timeout:10000})
			
			function geo_success(pos){
				d = distance(pos.coords.latitude, 
						pos.coords.longitude, 
						state.lat, 
						state.lng)

				// only update if the distance moved is greater than the move tolerance
				if ( d > state.d_buff ) {
					
					state.lat = pos.coords.latitude
					state.lng = pos.coords.longitude

					if (center_on_success || !state.initialized) 
						center_map(state)
					console.log('init: '+state.initialized)
					state.initialized = true

					subscribeToStrokes(state, add_stroke);
					state['update_locked'] = true
					setInterval(function(){state.update_locked = false},30000)
				}
			}

			function geo_error(err) {
				// TODO make a better error
				console.log('GEO LOCATION ERROR');
				console.log(err);
			}
		} 
		else {
			console.log('NO GEOLOCATION')
			// geolocation is not supported
		}
	}
}

function center_map(state) {
	state.map.setOptions({
		center:new google.maps.LatLng(state.lat, state.lng)
	})
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
		center:new google.maps.LatLng(state.lat, state.lng), zoom:18,
		mapTypeId:google.maps.MapTypeId.ROADMAP,
		styles: myStyles	
	};
		
	var map = new google.maps.Map(document.getElementById("map-container"),mapOptions);
	state['map'] = map;

	update_geolocation(state,true)

	map.addListener('click',function(e) { } );
	map.addListener('mouseup',function(e) { finish_stroke(state) } );
	map.addListener('mousedown',function(e) { state['pointer'] = 'down'; update_strokes(state) } )
	map.addListener('mousemove',function(e) { build_stroke(state,e); } )

	map.setOptions({draggable: false, zoomControl: false, scrollwheel: false, disableDoubleClickZoom: true, clickableIcons: false});

	return map
}

function finish_stroke(state) {

	if (state.mode != 'draw') return;
	
	// Finish building stroke
	var curr_stroke = state.curr_stroke;
	if (curr_stroke.path.length == 0) return;
	state.curr_stroke = init_stroke(state)
	state['pointer'] = 'up';

	// Build stroke pkg
	var stroke_data = {
		'path': curr_stroke.path,
		'type': curr_stroke.type,
		'tags': curr_stroke.tags,
		'user_id': curr_stroke.user_id,
		'timestamp': Date.now(),
		'color': curr_stroke.color,
		'weight': curr_stroke.weight
	}
	stroke = saveStroke(stroke_data);		
	state.stroke_paths[stroke.id] = stroke
	update_strokes(state)
}

function build_stroke(state,e) {
	// this might not be the right place for this, 
	// bc it might not update to often

	lat = e.latLng.lat()
	lng = e.latLng.lng()
	d = distance(lat,lng,state.lat, state.lng)
	if (state.pointer == 'down' && state.mode == 'draw' && d < state.rad) {
		// when starting a new stroke, update geolocation
		if (state.curr_stroke.path.length == 0) update_geolocation(state,false);

		state.curr_stroke.path.push({lat: lat, lng: lng})

		// update map
		if('curr_stroke_obj' in state) state['curr_stroke_obj'].setMap(null)
		state['curr_stroke_obj'] = build_stroke_path(state, state.curr_stroke)

		state.curr_stroke_obj.setMap(state.map);

	}
	if (d >= state.rad) {
		// end the current stroke if you draw beyond the edge of the boundary
		finish_stroke(state)
	}
}

function distance(x1, y1, x2, y2) {
	return Math.pow(Math.pow(x2-x1,2)+Math.pow(y2-y1,2),0.5)
}

function update_strokes(state) {

	// REMOVE existing strokes from map
	var n_redrawn = 0
	for(var id in state.stroke_paths){
		stroke = state.stroke_paths[id]
		if ( !(id in state.strokes)) {
			draw_stroke(state,stroke)
			n_redrawn += 1
		}
	}
	draw_stroke(state,state.curr_stroke)
}

function build_stroke_path(state,stroke) {
	return new google.maps.Polyline({
		clickable:false,
		path: stroke.path,
		geodesic: true,
		strokeColor: stroke.color,
		strokeOpacity: 1.0,
		strokeWeight: stroke.weight
	});
}

function draw_stroke(state, stroke) {
	path = build_stroke_path(state,stroke)
	state.strokes[stroke.id] = path
	path.setMap(state.map);
}

function obj_len(obj) {
	var ctr = 0
	for (key in obj) ctr += 1
	return ctr
}

