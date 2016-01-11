(function ( $ ) {
 
	var ellipsisWidth = 64;
	var topTolerance = 10;
	
    $.fn.oneline = function(alignment) {
    	this.css("white-space", "normal");
    	var children = this.children();
    	var thisRight = this.offset().left + this.width();
    	var thisTop = this.offset().top;
    	var hide = false;
    	for (var i=0; i<children.length; i++) {
    		var $child = $(children[i]);
    		if (hide) {
    			$child.hide();
    		} else if ($child.offset().left + $child.outerWidth() + ellipsisWidth > thisRight
	    				|| $child.offset().top - thisTop > topTolerance) {
	    		hide = true;
	    	}
    	}
    	var $ellipsis = jQuery("<a class='ellipis'>...</a>");
    	this.append($ellipsis);
    	var $container = this;
    	$ellipsis.click(function() {
    		$container.children().show();
    		$ellipsis.remove();
    	});
    	return this;
    };
 
}( jQuery ));
