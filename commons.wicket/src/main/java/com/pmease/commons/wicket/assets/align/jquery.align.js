(function ( $ ) {
 
	var pageMargin = 30;
	var scrollbarWidth = 20;
	var textHeight = 25;
	
	/**
	 * @parameter alignment
	 * 			alignment should be in form of {placement, target}, where placement represents relative position 
	 * 			of aligned element to target element in form of {x, y, targetX, targetY}, and target should be 
	 * 			either a region in form of {left, top, width, height}, or an DOM element in form of {element}, 
	 * 			the target may also includes a index property to denote which character to align with in target 
	 *          input/textarea element
	 */
    $.fn.align = function(alignment) {
    	var $parent = this.parent();
    	if ($parent.css("position") != "relative") {
    		$parent.css("position", "relative");
    	}
    	if(this.css("position") != "absolute") {
    		this.css("position", "absolute");
    	}

    	var autoSize = this.find(".autosize");
    	if (autoSize.length == 0)
    		autoSize = this;
    	
    	var scrollTop = autoSize.scrollTop();
    	var scrollLeft = autoSize.scrollLeft();
    	
    	// reset width and height as otherwise the dimension will not change
    	// even if enclosed content changes
    	autoSize.css("width", "auto");
    	autoSize.css("height", "auto");
    	autoSize.css("overflow", "auto");
    	
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
    		var exceeded = 0;
    		if (top < borderTop)
    			exceeded += borderTop - top;
    		if (top+height > borderBottom)
    			exceeded += top+height-borderBottom;
    		
    		// flip  to see if situation is better
    		anchor = targetHeight*(100-alignment.placement.targetY)/100.0 + targetTop;
    		top = anchor - (100 - alignment.placement.y) * height / 100.0;

    		if (alignment.placement.targetY == 0 && alignment.placement.y == 100)
        		top += offset;
        	else if (alignment.placement.targetY == 100 && alignment.placement.y == 0)
        		top -= offset;
    		
    		if (top < borderTop || top + height> borderBottom) {
        		var exceededAfterFlip = 0;
        		if (top < borderTop)
        			exceededAfterFlip += borderTop - top;
        		if (top+height > borderBottom)
        			exceededAfterFlip += top+height-borderBottom;

        		if (exceededAfterFlip > exceeded) {
	    			// situation is even worse after flip, so we revert back
	    			anchor = targetHeight*alignment.placement.targetY/100.0 + targetTop;
	    			top = anchor - alignment.placement.y*height/100.0;
	    	    	if (alignment.placement.targetY == 0 && alignment.placement.y == 100)
	    	    		top -= offset;
	    	    	else if (alignment.placement.targetY == 100 && alignment.placement.y == 0)
	    	    		top += offset;
        		}
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

    	anchor = targetWidth*alignment.placement.targetX/100.0 + targetLeft;
    	left = anchor - alignment.placement.x*width/100.0;
    	if (alignment.placement.targetX == 0 && alignment.placement.x == 100)
    		left -= offset;
    	else if (alignment.placement.targetX == 100 && alignment.placement.x == 0)
    		left += offset;
    		
    	if (left < borderLeft || left + width > borderRight) {
    		var exceeded = 0;
    		if (left < borderLeft)
    			exceeded += borderLeft-left;
    		if (left+width > borderRight)
    			exceeded += left+width-borderRight;
    		
    		// flip to see if situation is better
    		anchor = targetWidth*(100-alignment.placement.targetX)/100.0 + targetLeft;
    		left = anchor - (100 - alignment.placement.x) * width / 100.0;

    		if (alignment.placement.targetX == 0 && alignment.placement.x == 100)
        		left += offset;
        	else if (alignment.placement.targetX == 100 && alignment.placement.x == 0)
        		left -= offset;
    		
    		if (left < borderLeft || left + width > borderRight) {
        		var exceededAfterFlip = 0;
        		if (left < borderLeft)
        			exceededAfterFlip += borderLeft-left;
        		if (left+width > borderRight)
        			exceededAfterFlip += left+width-borderRight;
        		
    			// situation is even worse after flip, so we revert back
        		if (exceededAfterFlip > exceeded) {
        			anchor = targetWidth*alignment.placement.targetX/100.0 + targetLeft;
        			left = anchor - alignment.placement.x*width/100.0;
        	    	if (alignment.placement.targetX == 0 && alignment.placement.x == 100)
        	    		left -= offset;
        	    	else if (alignment.placement.targetX == 100 && alignment.placement.x == 0)
        	    		left += offset;
        		}
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
    	
    	this.css({left:left-$parent.offset().left, top:top-$parent.offset().top});	
    	
    	if (height < thisHeight)
        	autoSize.outerHeight(autoSize.outerHeight()+height-thisHeight);
    	
    	if (width < thisWidth)
    		autoSize.outerWidth(autoSize.outerWidth()+width-thisWidth);

    	if (autoSize.height() < autoSize[0].scrollHeight)
    		autoSize.outerWidth(autoSize.outerWidth()+scrollbarWidth);
    	
    	autoSize.scrollTop(scrollTop);
    	autoSize.scrollLeft(scrollLeft);
    	
    	return this;
    };
 
}( jQuery ));
