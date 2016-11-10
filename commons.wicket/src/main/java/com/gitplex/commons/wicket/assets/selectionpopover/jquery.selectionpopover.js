(function ( $ ) {
    $.fn.selectionPopover = function(action, options) {
    	var $container = jQuery(this);
    	$container.css("position", "relative");
    	function popover(options) {
			var $popover = $container.children(".selection-popover");
			if (options === "close") {
				$popover.remove();
			} else if (options) {
    			if ($popover.length == 0) {
                	$popover = $("" +
            				"<div class='selection-popover'>" +
            				"<div class='content'></div>" +
            				"<div class='triangle'></div>" +
            				"</div>");
                	$container.append($popover);
    			}
    			var $content = $popover.children(".content");
    			$content.empty();
    			$content.append(options.content);
            	var left = options.position.left - $popover.outerWidth()/2;
            	var top = options.position.top - $popover.outerHeight();
            	if (left < $(window).scrollLeft())
            		left = $(window).scrollLeft();
            	if (left + $popover.outerWidth() > $(window).width() + $(window).scrollLeft())
            		left = $(window).width() + $(window).scrollLeft() - $popover.outerWidth();
            	if (top < $(window).scrollTop())
            		top = $(window).scrollTop();
            	if (top + $popover.outerHeight() > $(window).height() + $(window).scrollTop())
            		top = $(window).height() + $(window).scrollTop() - $popover.outerHeight();
            	if (top + $popover.outerHeight() > options.position.top+10)
            		$popover.children(".triangle").hide();
            	$popover.css({
            		left: left-$container.offset().left,
            		top: top-$container.offset().top
            	});
    		}
    	}

    	if (action === "init") {
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
    	} else if (action === "open") {
    		popover(options);
    	} else if (action === "close") {
    		popover("close");
    	}
    	
    	return this;
    };
 
}( jQuery ));

$(function() {
	$(document).keydown(function(e) {
		if (e.keyCode == 27) {
			$(".selection-popover").remove();
		}
	});
});