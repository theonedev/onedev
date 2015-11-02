pmease.commons.floating = {
	init: function(floatingId, targetId, alignment, callback) {
		var $floating = $("#" + floatingId);
		var $target = $("#" + targetId);
		$target.addClass("alignment-target");

		alignment.target = $target;
		alignment.resizable = ">.content";
		$floating.data("alignment", alignment);
		$floating.align(alignment);
		
		$floating.data("mouseUpOrTouchStart") = function(e) {
		    if (!$floating.is(e.target) // if the target of the click isn't the container...
		            && $floating.has(e.target).length === 0) { // ... nor a descendant of the container
		    	pmease.commons.floating.close(floatingId, callback);
		    }
		};
		$(document).on("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
		
		$floating.data("keydown") = function(e) {
			if (e.keyCode == 27) { // esc
				if ($(".select2-drop:visible").length == 0) {
					var topmostPopup = $("body>.popup:visible:last");
					if (topmostPopup[0]) {
						if (topmostPopup.hasClass("modal")) {
							if (!topmostPopup[0].confirm)
								pmease.commons.modal.hide(topmostPopup[0].id);
							else
								topmostPopup.modal("hide").remove();
						}
						if (topmostPopup.hasClass("dropdown-panel"))
							pmease.commons.dropdown.hide(topmostPopup[0].id);
					}
				}
			} else if (e.keyCode == 13) {
				var topmostPopup = $("body>.popup:visible:last");
				if (topmostPopup[0] && topmostPopup[0].confirm) {
					topmostPopup[0].confirm.callback();
					topmostPopup.modal("hide").remove();
				}
			}
		}
		
		// use keydown as keypress does not work in chrome/safari
		$(document).on("keydown", $floating.data("keydown"));
	}, 
	
	close: function(floatingId, callback) {
		var $floating = $("#" + floatingId);
		
		$(document).off("mouseup touchstart", $floating.data("mouseUpOrTouchStart"));
		$(document).off("keydown", $floating.data("keydown"));
		
		$floating.remove();
		if (callback)
			callback();
	}
}