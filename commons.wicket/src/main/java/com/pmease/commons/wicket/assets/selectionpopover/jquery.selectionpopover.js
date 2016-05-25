(function ( $ ) {
 
    $.fn.selectionPopover = function(action, options) {
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
            	$popover.children(".content").append(options.content);

            	$("body").append($popover);
            	$popover.data("container", $container[0]);

    			var alignment = {
    					target: {left: options.position.left, top: options.position.top, width: 0, height: 0}, 
    					placement: {x: 50, y: 100, targetX: 0, targetY: 0}};
    			$popover.children(".triangle").show();
    			$popover.show().align(alignment);
    			if (Math.abs($popover.offset().top+$popover.outerHeight() - options.position.top)>10) {
    				$popover.children(".triangle").hide();
    			} 
    			$container.data("selectionPopover", $popover[0]);
    		}
    	}

    	if (action == "init") {
    	    $container.on("mouseup", function() {
    			// use a timeout to make sure selection remains stable after mouse or keyboard action
    	    	setTimeout(function() {
        	    	popover(options());
    	    	}, 100);
    	    });
    	    $container.on("keyup", function(e) {
    	    	if (e.which == 37 || e.which == 38 || e.which == 39 || e.which == 40) {
        			// use a timeout to make sure selection remains stable after mouse or keyboard action
        	    	setTimeout(function() {
            	    	popover(options());
        	    	}, 100);
    	    	}
    	    });
    	} else if (action == "open") {
    		popover(options);
    	} else {
    		popover();
    	}
    	
    	return this;
    };
 
}( jQuery ));

$(function() {
	Wicket.Event.subscribe('/ajax/call/complete', function() {
		$("body>.selection-popover").each(function() {
			var $popover = $(this);
			if (!jQuery.contains(document, $popover.data("container"))) {
				$popover.remove();
			}
		});
	});  
	$(document).keydown(function(e) {
		if (e.keyCode == 27) {
			$("body>.selection-popover").remove();
		}
	});
});