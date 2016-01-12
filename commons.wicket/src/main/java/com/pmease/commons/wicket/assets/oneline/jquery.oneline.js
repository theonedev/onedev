(function ( $ ) {
 
    $.fn.oneline = function(expanderSelector, topMargin, rightMargin) {
    	this.css("white-space", "normal");
    	var children = this.children();
    	var thisRight = this.offset().left + this.width();
    	var thisTop = this.offset().top;
    	var hide = false;
    	for (var i=0; i<children.length; i++) {
    		var $child = $(children[i]);
    		if (hide) {
    			$child.hide();
    		} else if ($child.offset().left + $child.outerWidth() + rightMargin > thisRight
	    				|| $child.offset().top - thisTop > topMargin) {
	    		hide = true;
	    	}
    	}
		var $expander = jQuery(expanderSelector);
    	if (hide) {
    		$expander.show();
        	this.append($expander);
        	var $container = this;
        	$expander.click(function() {
        		$container.children().show();
        		$expander.hide();
        	});
    	} else {
    		$expander.hide();
    	}
    	return this;
    };
 
}( jQuery ));
