pmease.commons.floating = {
	init: function(floatingId, targetId, alignment, closeCallback) {
		var $floating = $("#" + floatingId);
		$floating.data("closeCallback", closeCallback);
		
		$floating.data("mouseUpOrTouchStart", function(e) {
		    if (!$floating.is(e.target) // if the target of the click isn't the container...
		            && $floating.has(e.target).length === 0) { // ... nor a descendant of the container
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
		
		$floating.data("ajaxCallComplete", function() {
			var $floating = $("body>.floating");
			if ($floating.data("alignment"))
				$floating.align($floating.data("alignment"));
		});
		
		Wicket.Event.subscribe("/ajax/call/complete", $floating.data("ajaxCallComplete"));

		alignment.target = $("#" + targetId);
		alignment.target.addClass("floating-aligned");
		
		alignment.resizable = ">.content";
		$floating.data("alignment", alignment);

		$floating.align(alignment);
	}, 
	
	close: function($floating, callCloseCallback) {
		if (callCloseCallback) 
			$floating.data("closeCallback")();
		
		$floating.data("alignment").target.removeClass("floating-aligned");
		
		$(document).off("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
		$(document).off("keydown", $floating.data("keydown"));
		Wicket.Event.unsubscribe("/ajax/call/complete", $floating.data("ajaxCallComplete"));
		
		$floating.remove();
	}
}