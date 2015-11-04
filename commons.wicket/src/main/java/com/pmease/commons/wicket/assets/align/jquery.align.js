/*
 * 2014, Robin Shen
 */
(function ( $ ) {
 
	var pageMargin = 8;
	var triangleSize = 6;
	
    $.fn.align = function( alignment ) {
    	if (!this.parent().is("body")) 
    		jQuery("body").append(this);
    	if(this.css("position") != "absolute")
    		this.css("position", "absolute");
    	
    	var borderTop = jQuery(window).scrollTop() + pageMargin;
    	var borderBottom = borderTop + jQuery(window).height() - 2*pageMargin;
    	var borderLeft = jQuery(window).scrollLeft() + pageMargin;
    	var borderRight = borderLeft + jQuery(window).width() - 2*pageMargin;

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

    	if (alignment.triangle) {
    		var $triangles = this.find(">.align-triangle"); 
    		if ($triangles.length == 0) {
				this.prepend("<div class='align-triangle'></div>");
				this.append("<div class='align-triangle'></div>");
				$triangles = this.find(">.align-triangle");				
    		}

        	$triangles.removeClass("left right top bottom");
    		if (left >= targetLeft + targetWidth) {
    			if (left + width + alignment.offset <= borderRight) {
    				$triangles.addClass("right");
    				var targetCenter = targetTop + targetHeight/2.0;
    				var triangleTop = targetCenter - top - triangleSize;
    				if (triangleTop < triangleSize)
    					triangleTop = triangleSize;
    				if (triangleTop > height - 3 * triangleSize)
    					triangleTop = height - 3 * triangleSize;
    				$($triangles[0]).css({top: (triangleTop - 1) + "px"});
    				$($triangles[1]).css({top: triangleTop + "px"});
    			}
    		} else if (left + width <= targetLeft) {
    			if (left - alignment.offset >= borderLeft) {
    				$triangles.addClass("left");
    				var targetCenter = targetTop + targetHeight/2.0;
    				var triangleTop = targetCenter - top - triangleSize;
    				if (triangleTop < triangleSize)
    					triangleTop = triangleSize;
    				if (triangleTop > height - 3 * triangleSize)
    					triangleTop = height - 3 * triangleSize;
    				$($triangles[0]).css({top: (triangleTop - 1) + "px"});
    				$($triangles[1]).css({top: triangleTop + "px"});
    			}
    		} else if (top >= targetTop + targetHeight) {
    			if (top + height + alignment.offset <= borderBottom) {
    				$triangles.addClass("bottom");
    				var targetCenter = targetLeft + targetWidth/2.0;
    				var triangleLeft = targetCenter - left - triangleSize;
    				if (triangleLeft < triangleSize)
    					triangleLeft = triangleSize;
    				if (triangleLeft > width - 3 * triangleSize)
    					triangleLeft = width - 3 * triangleSize;
    				$($triangles[0]).css({left: (triangleLeft - 1) + "px"});
    				$($triangles[1]).css({left: triangleLeft + "px"});
    			}
    		} else if (top + height <= targetTop) {
    			if (top - alignment.offset >= borderTop) {
    				$triangles.addClass("top");
    				var targetCenter = targetLeft + targetWidth/2.0;
    				var triangleLeft = targetCenter - left - triangleSize;
    				if (triangleLeft < triangleSize)
    					triangleLeft = triangleSize;
    				if (triangleLeft > width - 3 * triangleSize)
    					triangleLeft = width - 3 * triangleSize;
    				$($triangles[0]).css({left: (triangleLeft - 1) + "px"});
    				$($triangles[1]).css({left: triangleLeft + "px"});
    			}
    		}
    	}
    	
    	this.css({left:left, top:top});	
    	if (width < this.outerWidth() || height < this.outerHeight())
        	this.outerWidth(width).outerHeight(height).css({overflow: "auto", "border-radius": "0"});
    	
    	return this;
    };
 
}( jQuery ));
