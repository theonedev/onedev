/*
 * 2014, Robin Shen
 */
(function ( $ ) {
 
    $.fn.align = function( alignment ) {
 
    	jQuery("body").append(this);
    	
    	this.css("position", "absolute");
    	
    	var margin = 8;
    	var borderTop = jQuery(window).scrollTop() + margin;
    	var borderBottom = borderTop + jQuery(window).height() - 2*margin;
    	var borderLeft = jQuery(window).scrollLeft() + margin;
    	var borderRight = borderLeft + jQuery(window).width() - 2*margin;

    	var width = this.outerWidth();
    	var height = this.outerHeight();
    	
    	var left, top;
    	var targetLeft, targetTop;
    	var targetWidth, targetHeight;
    	var offset = 0;
    	
    	if (alignment.offset)
    		offset = alignment.offset;

    	if (alignment.target instanceof jQuery || alignment.target instanceof HTMLElement) {
    		var $targetEl = jQuery(alignment.target);
    		targetLeft = $targetEl.offset().left;
    		targetTop = $targetEl.offset().top;
    		targetWidth = $targetEl.outerWidth();
    		targetHeight = $targetEl.outerHeight();
    	} else {
    		targetLeft = alignment.target.left;
    		targetTop = alignment.target.top;
    		targetWidth = alignment.target.width;
    		targetHeight = alignment.target.height;
    	}
    	
    	var anchor = targetWidth*alignment.targetX/100.0 + targetLeft;
    	left = anchor - alignment.x*width/100.0;
    	if (alignment.targetX == 0 && alignment.x == 100)
    		left -= offset;
    	else if (alignment.targetX == 100 && alignment.x == 0)
    		left += offset;
    		
    	if (left < borderLeft || left + width > borderRight) {
    		// flip  to see if width can fit into screen
    		anchor = targetWidth*(100-alignment.targetX)/100.0 + targetLeft;
    		left = anchor - (100 - alignment.x) * width / 100.0;

    		if (alignment.targetX == 0 && alignment.x == 100)
        		left += offset;
        	else if (alignment.targetX == 100 && alignment.x == 0)
        		left -= offset;
    		
    		if (left < borderLeft || left + width > borderRight) {
    			// does not fit even flipped, so we revert back
    			anchor = targetWidth*alignment.targetX/100.0 + targetLeft;
    			left = anchor - alignment.x*width/100.0;
    	    	if (alignment.targetX == 0 && alignment.x == 100)
    	    		left -= offset;
    	    	else if (alignment.targetX == 100 && alignment.x == 0)
    	    		left += offset;
    		}
    		
    		if (alignment.targetY==100 && alignment.y==0 
    				|| alignment.targetY==0 && alignment.y==100) {
        		if (left + width > borderRight)
        			left = borderRight - width;
        		if (left < borderLeft)
        			left = borderLeft;
    		}
    		
    		// last resort: resize element
    		if (left + width > borderRight) 
    			width = borderRight - left;
    		if (left < borderLeft) {
    			left = borderLeft;
    			width -= borderLeft - left;
    		}
    	}
    	
    	anchor = targetHeight*alignment.targetY/100.0 + targetTop;
    	top = anchor - alignment.y*height/100.0;
    	if (alignment.targetY == 0 && alignment.y == 100)
    		top -= offset;
    	else if (alignment.targetY == 100 && alignment.y == 0)
    		top += offset;
    		
    	if (top < borderTop || top + height > borderBottom) {
    		// flip  to see if height can fit into screen
    		anchor = targetHeight*(100-alignment.targetY)/100.0 + targetTop;
    		top = anchor - (100 - alignment.y) * height / 100.0;

    		if (alignment.targetY == 0 && alignment.y == 100)
        		top += offset;
        	else if (alignment.targetY == 100 && alignment.y == 0)
        		top -= offset;
    		
    		if (top < borderTop || top + height> borderBottom) {
    			// does not fit even flipped, so we revert back
    			anchor = targetHeight*alignment.targetY/100.0 + targetTop;
    			top = anchor - alignment.y*height/100.0;
    	    	if (alignment.targetY == 0 && alignment.y == 100)
    	    		top -= offset;
    	    	else if (alignment.targetY == 100 && alignment.y == 0)
    	    		top += offset;
    		}
    		
    		if (alignment.targetX==100 && alignment.x==0 
    				|| alignment.targetX==0 && alignment.x==100) {
        		if (top + height > borderBottom)
        			top = borderBottom - height;
        		if (top < borderTop)
        			top = borderTop;
    		}
    		
    		// last resort: resize element
    		if (top + height > borderBottom) 
    			height = borderBottom - top;
    		if (top < borderTop) {
    			top = borderTop;
    			height -= borderTop - top;
    		}
    	}

    	this.css({left:left, top:top});	
    	if (alignment.resizable) {
    		var $resizable = this.find(alignment.resizable);
    		$resizable.outerWidth(width - this.outerWidth() + $resizable.outerWidth());
    		$resizable.outerHeight(height - this.outerHeight() + $resizable.outerHeight());
    		$resizable.css({overflow: "auto"});
    	} else {
    		this.outerWidth(width).outerHeight(height).css({overflow: "auto"});
    	}
    	
    	return this;
    };
 
}( jQuery ));
