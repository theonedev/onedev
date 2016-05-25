(function ( $ ) {
 
    $.fn.selectionPopover = function(callback) {
    	var $container = jQuery(this);
    	
    	function popover(options) {
    		var popover = $container.data("selectionPopover");
    		if (popover && jQuery.contains(document, popover)) {
    			$(popover).remove();
    		}
    		if (options) {
            	var $popover = $("" +
        				"<div class='selection-popover'>" +
        				"<div class='content'></div>" +
        				"<div class='triangle'></div>" +
        				"</div>");
            	$popover.append(options.content);

            	$("body").append($popover);

    			var alignment = {
    					target: {left: options.position.left, top: options.position.top, width: 0, height: 0}, 
    					placement: {x: 50, y: 100, targetX: 0, targetY: 0}};
    			$popover.children(".triangle").show();
    			$popover.show().align(alignment);
    			if (Math.abs($popover.offset().top+$popover.outerHeight() - position.top)>10) {
    				$popover.children(".triangle").hide();
    			} 
    			/*
        		Wicket.Event.subscribe('/ajax/call/complete', function() {
        			if (!jQuery.contains(document, $container[0])) {
        				$popover.remove();
        			}
        		});   
        		*/
    			$container.data("selectionPopover", $popover[0]);
    		}
    	}

	    $container.on("mouseup", function() {
	    	popover(callback());
	    });
	    $container.on("keyup", function(e) {
	    	if (e.which == 37 || e.which == 38 || e.which == 39 || e.which == 40) {
	    		popover(callback());
	    	}
	    });
    	
    	return this;
    };
 
}( jQuery ));
