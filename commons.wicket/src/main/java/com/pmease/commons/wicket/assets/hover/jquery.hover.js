(function ( $ ) {
 
	var hoverDelay = 350;

	/**
	 * @parameter callback 
	 * 			should return an element to be shown when mouse hovers on the trigger, returned element 
	 * 			should already been added to DOM
	 * @parameter alignment
	 * 			alignment should be in form of {targetX, targetY, x, y} 
	 */
	$.fn.hover = function(callback, alignment) {
		var $trigger = this;
		var popover;
		var showTimer;
		var hideTimer;

		function show() {
			if (!popover) {
				popover = callback();
				
				var $popover = jQuery(popover);
				
				$popover.mouseover(function() {
					cancelHide();
				});
				
				$popover.mouseout(function() {
					cancelShow();
					cancelHide();
					hideTimer = setTimeout(hide, hoverDelay);
				});

				$popover.mousemove(function() {
					cancelHide();
				});
				
				$popover.align({
					placement: alignment,
					target: {element: $trigger[0]}
				});
			}
		}

		function hide() {
			if (popover) {
				jQuery(popover).remove();
				popover = undefined;
			}
			cancelShow();
			cancelHide();
		}
		
		function cancelShow() {
			if (showTimer) {
				clearTimeout(showTimer);
				showTimer = undefined;
			}
		}
		
		function cancelHide() {
			if (hideTimer) {
				clearTimeout(hideTimer);
				hideTimer = undefined;
			}
		}
		
		$trigger.mouseover(function() {
			cancelShow();
			cancelHide();
			showTimer = setTimeout(show, hoverDelay);
		});
		
		$trigger.mouseout(function() {
			cancelShow();
			cancelHide();
			hideTimer = setTimeout(hide, hoverDelay);
		});

		$trigger.mousemove(function() {
			cancelHide();
		});
		
    	return this;
    };
 
}( jQuery ));
