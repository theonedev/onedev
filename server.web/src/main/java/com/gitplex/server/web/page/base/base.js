gitplex.server = {};
gitplex.server.mouseState = {
	pressed: false, 
	moved: false
};

$(document).ready(function() {
	$(window).load(function() {
		$(document).mousedown(function() { 
			gitplex.server.mouseState.pressed = true;
			gitplex.server.mouseState.moved = false;
		});
		$(document).mouseup(function() {
			gitplex.server.mouseState.pressed = false;
			gitplex.server.mouseState.moved = false;
		});	
		$(document).mousemove(function(e) {
			// IE fires mouse move event after mouse click sometimes, so we check 
			// if mouse is really moved here
			if (e.clientX != self.clientX || e.clientY != self.clientY) {
				gitplex.server.mouseState.moved = true;
				self.clientX = e.clientX;
				self.clientY = e.clientY;
			}
		});
		$(document).scroll(function() {
			gitplex.server.mouseState.moved = false;
		});
	});
});
