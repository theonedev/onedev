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
	}
};

$(document).ready(function() {
	$(window).load(function() {
		$(document).mousedown(function() { 
			gitplex.mouseState.pressed = true;
		});
		$(document).mouseup(function() {
			gitplex.mouseState.pressed = false;
		});	
		$(document).mouseMove(function() {
			gitplex.mouseState.moved = true;
		});
		$(document).scroll(function() {
			gitplex.mouseState.moved = false;
		});
		gitplex.expandable.check();
	});
});
