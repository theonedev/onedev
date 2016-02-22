/*
 * 2015, Robin Shen
 */
(function ( $ ) {
 
	var defaultTopMargin = defaultBottomMargin = 8;
	
    $.fn.scrollIntoView = function(selector, topMargin, bottomMargin) {
    	if (topMargin == undefined)
    		topMargin = defaultTopMargin;
    	if (bottomMargin == undefined)
    		bottomMargin = defaultBottomMargin;
    	
		var $container = jQuery(this);
		var $active = $container.find(selector);
		
		if ($active.length != 0) {
			var containerTop = $container.offset().top;
			var activeTop = $active.offset().top;
			if (activeTop-topMargin<containerTop)
				$container.scrollTop($container.scrollTop()-(containerTop-activeTop+topMargin));

			var containerBottom = containerTop + $container.height();
			var activeBottom = activeTop + $active.height();
			if (activeBottom+bottomMargin>containerBottom) 
				$container.scrollTop($container.scrollTop()+(activeBottom+bottomMargin-containerBottom));
		}
    	
    	return this;
    };
 
}( jQuery ));
