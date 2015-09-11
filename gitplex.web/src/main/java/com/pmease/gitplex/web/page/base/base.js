var gitplex = {
	utils: {
		/**
		 * Trigger a callback when the selected images are loaded:
		 * @param {String} selector
		 * @param {Function} callback
		 */
		 onImageLoad: function(selector, callback){
		    $(selector).each(function(){
		        if (this.complete || /*for IE 10-*/ $(this).height() > 0) {
		            callback.apply(this);
		        }
		        else {
		            $(this).on('load', function(){
		                callback.apply(this);
		            });
		        }
		    });
		}
	}, 
	
	expandable: {
		check: function() {
			if (gitplex.expandable.getScrollTop && !gitplex.mouseState.pressed && $(".CodeMirror-fullscreen").length == 0) {
				var scrollTop = gitplex.expandable.getScrollTop();
				var $hideable = $(".hideable");
				if ($hideable.is(":visible")) {
					var height = 0;
					var topHeight = 0;
					$hideable.each(function() {
						if ($(this).hasClass("top"))
							topHeight += $(this).outerHeight();
						height += $(this).outerHeight();
					});
					if (scrollTop>height+10) {
						$hideable.hide();
						$(window).resize();
						gitplex.expandable.setScrollTop(scrollTop-topHeight);
					}
				} else if (scrollTop < 5) {
					$hideable.show();
					$(window).resize();
				}
			}
			setTimeout(gitplex.expandable.check, 100);
		}
	},
	
	mouseState: {
		pressed: false, 
		moved: false
	},
	
	codemirror: {
		initState: function(cm, stateStr) {
		    // use timer to minimize performance impact 
		    var cursorTimer;
		    cm.on("cursorActivity", function() {
	    		if (cursorTimer)
	    			clearTimeout(cursorTimer);
	    		cursorTimer = setTimeout(function() {
	    			cursorTimer = undefined;
	    			var cursor = cm.getCursor();
			    	pmease.commons.history.setCursor(cursor);
			    	$(".preserve-cm-state").each(function() {
			    		var state = $(this).data("state");
			    		if (!state)
			    			state = {};
			    		state.cursor = cursor;
			    		$(this).data("state", state);
			    	});
		    	}, 500);
		    });
		    
		    var cursor = pmease.commons.history.getCursor();
		    if (cursor)
		    	cm.setCursor(cursor);
		    
		    // use timer to minimize performance impact 
		    var scrollTimer;
		    cm.on("scroll", function() {
		    	gitplex.mouseState.moved = false;			    	
		    	if (scrollTimer)
		    		clearTimeout(scrollTimer);
		    	scrollTimer = setTimeout(function() {
		    		scrollTimer = undefined;
			    	var scrollInfo = cm.getScrollInfo();
			    	var scroll = {left: scrollInfo.left, top: scrollInfo.top};
			    	pmease.commons.history.setScroll(scroll);
			    	$(".preserve-cm-state").each(function() {
			    		var state = $(this).data("state");
			    		if (!state)
			    			state = {};
			    		state.scroll = scroll;
			    		$(this).data("state", state);
			    	});
		    	}, 500);
		    });
		    var scroll = pmease.commons.history.getScroll();
		    if (scroll)
		    	cm.scrollTo(scroll.left, scroll.top);
		    
		    if (stateStr) {
		    	var state = JSON.parse(stateStr);
		    	if (state.cursor)
		    		cm.setCursor(state.cursor);
		    	if (state.scroll)
		    		cm.scrollTo(state.scroll.left, state.scroll.top);
		    }
		    
		}
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
		gitplex.expandable.check();
	});
});
