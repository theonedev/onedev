pmease.commons.floating = {
	init: function(floatingId, alignWith, alignment, closeCallback) {
		var $floating = $("#" + floatingId);
		$floating.data("closeCallback", closeCallback);
		
		$floating.data("mouseUpOrTouchStart", function(e) {
			/*
			 * Close the floating panel if mouse clicks outside of the floating. Also we 
			 * do not close the panel if mouse clicks on the element triggering this 
			 * floating panel, as normally the triggering element already has the logic 
			 * closing the floating when it is clicked (to achieve the toggle effect) 
			 */
		    if (!$floating.is(e.target) && $floating.has(e.target).length === 0 
		            && (!$floating.data("trigger") || !$floating.data("trigger").is(e.target))) { 
		    	pmease.commons.floating.close($floating, true);
		    }
		});
		$(document).on("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
		
		$floating.data("keydown", function(e) {
			if (e.keyCode == 27 && $(".select2-drop:visible").length == 0) 
				pmease.commons.floating.close($floating, true);
		});
		
		// use keydown as keypress does not work in chrome/safari
		$(document).on("keydown", $floating.data("keydown"));
		
		if (typeof alignWith === "string") {
			alignment.target = $("#" + alignWith);
			alignment.target.addClass("floating-aligned");
		} else {
			alignment.target = alignWith;
		}
		
		$floating.data("alignment", alignment);
		
		$floating.data("ajaxCallComplete", function() {
			$floating.show().align(alignment).trigger("open");
		});
		Wicket.Event.subscribe("/ajax/call/complete", $floating.data("ajaxCallComplete"));
	}, 
	
	close: function($floating, callCloseCallback) {
		if (callCloseCallback)
			$floating.data("closeCallback")();
		
		var alignment = $floating.data("alignment");
		
		if (alignment.target instanceof jQuery || alignment.target instanceof HTMLElement)
			alignment.target.removeClass("floating-aligned");
		
		$(document).off("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
		$(document).off("keydown", $floating.data("keydown"));
		Wicket.Event.unsubscribe("/ajax/call/complete", $floating.data("ajaxCallComplete"));
		
		$floating.trigger("close");
		
		$floating.remove();
	}
}