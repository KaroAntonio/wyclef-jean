var e = $('<div>');
e.appendTo('body'); 
e.css({
	'background':'white',
	'cursor':'pointer',
	'position':'absolute',
	'z-index':10000,
	'top':'10px',
	'left':'10px'
	}); 
e.html('^top'); 
e.hover(function(){ e.css({'text-decoration':'underline'}); },
		function(){ e.css({'text-decoration':'none'}); }); 
e.click(function(){ window.location = 'http://www.karoantonio.com'; });
