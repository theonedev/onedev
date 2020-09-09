(function ( $ ) {
    $.fn.selectionPopover = function(action, options) {

    	var $container = jQuery(this);
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
            	if (left < 0)
            		left = 0;
            	if (left + $popover.outerWidth() > $(window).width())
            		left = $(window).width() - $popover.outerWidth();
            	if (top < 0)
            		top = 0;
            	if (top + $popover.outerHeight() > $(window).height())
            		top = $(window).height() - $popover.outerHeight();
            	if (top + $popover.outerHeight() > options.position.top+10)
            		$popover.children(".triangle").hide();
            	$popover.css({left: left, top});
    		}
    	}

    	if (action === "init") {
    	    $container.on("mouseup", function(e) {
    			// use a timeout to make sure selection remains stable after mouse or keyboard action
    	    	setTimeout(function() {
        	    	popover(options(e));
    	    	}, 100);
    	    });
    	    $container.on("keyup", function(e) {
    	    	if (e.which == 37 || e.which == 38 || e.which == 39 || e.which == 40) {
        			// use a timeout to make sure selection remains stable after mouse or keyboard action
        	    	setTimeout(function() {
            	    	popover(options(e));
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
		if (e.keyCode == 27) 
			$(".selection-popover").remove();
	});
});