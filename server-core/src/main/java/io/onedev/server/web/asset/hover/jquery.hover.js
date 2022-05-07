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
		this.each(function() {
			var popover;
			var showTimer;
			var hideTimer;
			var trigger = this;

			function show() {
				if (!popover) {
					popover = callback.call(trigger);
					if (popover) {
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
							target: {element: trigger}
						});
						
						$popover.data("keydown", function(e) {
							if (e.keyCode == 27)
								hide();
						});
						
						jQuery(document).on("keydown", $popover.data("keydown"));
					}
				}
			}

			function hide() {
				if (popover) {
					var $popover = jQuery(popover);
					jQuery(document).off("keydown", $popover.data("keydown"));
					$popover.remove();
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
			
			jQuery(trigger).mouseover(function() {
				cancelShow();
				cancelHide();
				showTimer = setTimeout(show, hoverDelay);
			});
			
			jQuery(trigger).mouseout(function() {
				cancelShow();
				cancelHide();
				hideTimer = setTimeout(hide, hoverDelay);
			});

			jQuery(trigger).mousemove(function() {
				cancelHide();
			});
			
		});
		
    	return this;
    };
 
}( jQuery ));

