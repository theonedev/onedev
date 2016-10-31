var gitplex = {
	mouseState: {
		pressed: false, 
		moved: false
	}
};

$(document).ready(function() {
	$(window).load(function() {
		$(document).mousedown(function() { 
			gitplex.mouseState.pressed = true;
			gitplex.mouseState.moved = false;
		});
		$(document).mouseup(function() {
			gitplex.mouseState.pressed = false;
			gitplex.mouseState.moved = false;
		});	
		$(document).mousemove(function(e) {
			// IE fires mouse move event after mouse click sometimes, so we check 
			// if mouse is really moved here
			if (e.clientX != self.clientX || e.clientY != self.clientY) {
				gitplex.mouseState.moved = true;
				self.clientX = e.clientX;
				self.clientY = e.clientY;
			}
		});
		$(document).scroll(function() {
			gitplex.mouseState.moved = false;
		});
	});
});
