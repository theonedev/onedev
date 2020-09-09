onedev.server.floating = {
	init: function(floatingId, alignment, animation, closeCallback) {
		var $floating = $("#" + floatingId);
		$floating.data("closeCallback", closeCallback);
		$floating.data("animation", animation);
		
		$floating.data("mouseUpOrTouchStart", function(e) {
			/*
			 * Close the floating panel if mouse clicks outside of the floating. Also we 
			 * do not close the panel if mouse clicks on the element triggering this 
			 * floating panel, as normally the triggering element already has the logic 
			 * closing the floating when it is clicked (to achieve the toggle effect) 
			 */
			var x = e.pageX;
			var y = e.pageY;
			
			// add this extra check as otherwise clicking on scroll bar of floating will hide the 
			// floating in IE
			var contains = $floating.offset().left<x && $floating.offset().left+$floating.outerWidth()>x
					&& $floating.offset().top<y && $floating.offset().top+$floating.outerHeight()>y;
					
		    if (!$floating.is(e.target) && $floating.has(e.target).length === 0 && !contains) {
		    	var $trigger = $floating.data("trigger");
			    if (!$trigger || !$trigger.is(e.target) && $trigger.has(e.target).length === 0) {
					$floating.data("closeCallback")();
				}
		    }
		});
		$(document).on("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
		
		$floating.data("keydown", function(e) {
			if (e.keyCode == 27 && $(".select2-drop:visible").length == 0 
					&& $(".flatpickr-calendar.open").length == 0) {
				$floating.data("closeCallback")();
			}
		});
		
		// use keydown as keypress does not work in chrome/safari
		$(document).on("keydown", $floating.data("keydown"));

		if (alignment) {
			if (alignment.target.element)
				$(alignment.target.element).addClass("floating-aligned");
			$floating.data("alignment", alignment);
		}
		
		var openTriggered = false;
		$floating.data("afterElementReplace", function() {
			var alignment = $floating.data("alignment");
			if (alignment && alignment.target && alignment.target.element 
					&& !document.body.contains(alignment.target.element)) {
				$floating.data("closeCallback")();
			} else {
				if (alignment) {
					$floating.align(alignment);
					$floating.trigger("resized");
				}
				if (!openTriggered) {
					$floating.trigger("open");
					openTriggered = true;
				}
			}
		});
		$(document).on("afterElementReplace", $floating.data("afterElementReplace"));
	}, 
	
	close: function(floatingId) {
		var $floating = $("#" + floatingId);
		
		if ($floating.length != 0) {
			var alignment = $floating.data("alignment");
			
			if (alignment && alignment.target.element)
				$(alignment.target.element).removeClass("floating-aligned");
			
			$(document).off("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
			$(document).off("keydown", $floating.data("keydown"));
			$(document).off("afterElementReplace", $floating.data("afterElementReplace"));
			
			$floating.trigger("close");
			
			var animation = $floating.data("animation");
			if (animation) {
				$floating.hide("slide", {direction: animation.toLowerCase()}, 200, function() {
					$floating.remove();
				});
			} else {
				$floating.remove();
			}
		}
	}
	
};
