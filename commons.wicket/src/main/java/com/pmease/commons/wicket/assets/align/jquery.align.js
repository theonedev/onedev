/*
 * 2014, Robin Shen
 */
(function ( $ ) {
 
	var pageMargin = 8;
	var scrollbarWidth = 20;
	var textHeight = 25;
	
    $.fn.align = function(alignment) {
    	if (!this.parent().is("body")) 
    		jQuery("body").append(this);
    	if(this.css("position") != "absolute")
    		this.css("position", "absolute");
    	
    	// reset width and height as otherwise the dimension will not change
    	// even if enclosed content changes
    	this.css("width", "auto");
    	this.css("height", "auto");
    	
    	var borderTop = jQuery(window).scrollTop() + pageMargin;
    	var borderBottom = borderTop + jQuery(window).height() - 2*pageMargin;
    	var borderLeft = jQuery(window).scrollLeft() + pageMargin;
    	var borderRight = borderLeft + jQuery(window).width() - 2*pageMargin;

    	var thisWidth = this.outerWidth();
    	var thisHeight = this.outerHeight();
    	var width = thisWidth;
    	var height = thisHeight;
    	
    	var left, top;
    	var targetLeft, targetTop;
    	var targetWidth, targetHeight;
    	var offset = 0;
    	
    	if (alignment.placement.offset)
    		offset = alignment.placement.offset;

    	if (alignment.target.element) {
    		var $targetEl = jQuery(alignment.target.element);
    		if (alignment.target.index != undefined) {
				var coord = getCaretCoordinates(alignment.target.element, alignment.target.index);
    			targetLeft = coord.left + $targetEl.offset().left;
    			targetWidth = 0;
    			targetTop = coord.top + $targetEl.offset().top;
    			targetHeight = textHeight;
    		} else {
        		targetLeft = $targetEl.offset().left;
        		targetTop = $targetEl.offset().top;
        		targetWidth = $targetEl.outerWidth();
        		targetHeight = $targetEl.outerHeight();
    		}
    	} else {
    		targetLeft = alignment.target.left;
    		targetTop = alignment.target.top;
    		targetWidth = alignment.target.width;
    		targetHeight = alignment.target.height;
    	}
    	
    	var anchor = targetHeight*alignment.placement.targetY/100.0 + targetTop;
    	top = anchor - alignment.placement.y*height/100.0;
    	if (alignment.placement.targetY == 0 && alignment.placement.y == 100)
    		top -= offset;
    	else if (alignment.placement.targetY == 100 && alignment.placement.y == 0)
    		top += offset;
    		
    	if (top < borderTop || top + height > borderBottom) {
    		// flip  to see if height can fit into screen
    		anchor = targetHeight*(100-alignment.placement.targetY)/100.0 + targetTop;
    		top = anchor - (100 - alignment.placement.y) * height / 100.0;

    		if (alignment.placement.targetY == 0 && alignment.placement.y == 100)
        		top += offset;
        	else if (alignment.placement.targetY == 100 && alignment.placement.y == 0)
        		top -= offset;
    		
    		if (top < borderTop || top + height> borderBottom) {
    			// does not fit even flipped, so we revert back
    			anchor = targetHeight*alignment.placement.targetY/100.0 + targetTop;
    			top = anchor - alignment.placement.y*height/100.0;
    	    	if (alignment.placement.targetY == 0 && alignment.placement.y == 100)
    	    		top -= offset;
    	    	else if (alignment.placement.targetY == 100 && alignment.placement.y == 0)
    	    		top += offset;
    		}
    		
    		if (alignment.placement.targetX==100 && alignment.placement.x==0 
    				|| alignment.placement.targetX==0 && alignment.placement.x==100) {
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

    	// vertical scroll bar occupies width, and we increase width
    	// in order not to wrap lines unnecessarily
    	if (height < thisHeight) 
    		width += scrollbarWidth;
    	
    	anchor = targetWidth*alignment.placement.targetX/100.0 + targetLeft;
    	left = anchor - alignment.placement.x*width/100.0;
    	if (alignment.placement.targetX == 0 && alignment.placement.x == 100)
    		left -= offset;
    	else if (alignment.placement.targetX == 100 && alignment.placement.x == 0)
    		left += offset;
    		
    	if (left < borderLeft || left + width > borderRight) {
    		// flip  to see if width can fit into screen
    		anchor = targetWidth*(100-alignment.placement.targetX)/100.0 + targetLeft;
    		left = anchor - (100 - alignment.placement.x) * width / 100.0;

    		if (alignment.placement.targetX == 0 && alignment.placement.x == 100)
        		left += offset;
        	else if (alignment.placement.targetX == 100 && alignment.placement.x == 0)
        		left -= offset;
    		
    		if (left < borderLeft || left + width > borderRight) {
    			// does not fit even flipped, so we revert back
    			anchor = targetWidth*alignment.placement.targetX/100.0 + targetLeft;
    			left = anchor - alignment.placement.x*width/100.0;
    	    	if (alignment.placement.targetX == 0 && alignment.placement.x == 100)
    	    		left -= offset;
    	    	else if (alignment.placement.targetX == 100 && alignment.placement.x == 0)
    	    		left += offset;
    		}
    		
    		if (alignment.placement.targetY==100 && alignment.placement.y==0 
    				|| alignment.placement.targetY==0 && alignment.placement.y==100) {
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
    	
    	this.css({left:left, top:top});	
    	
    	var overflow = false;
    	if (height < thisHeight) {
        	this.outerHeight(height);
        	overflow = true;
    	}
    	if (overflow) {
    		// when vertical scroll bar appears, we adjust width to 
    		// avoid unnecessary line wrappings
    		this.outerWidth(width);
    	} else if (width < thisWidth) {
            this.outerWidth(width);
            overflow = true;
    	}
    	if (overflow)
        	this.css({"overflow": "auto"});
    	
    	return this;
    };
 
}( jQuery ));
