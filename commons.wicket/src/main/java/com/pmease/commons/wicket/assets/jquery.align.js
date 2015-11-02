/*
 * 2014, Robin Shen
 */
jQuery.fn.align = function(alignment) {
	if (this.length == 0)
		return this;
	
	// make sure position is absolute as otherwise width/height calculation
	// below might be inaccurate
	this.css("position", "absolute");
	
	if (alignment == undefined) 
		alignment = this.data("alignment");
	
	var borderTop = jQuery(window).scrollTop();
	var borderBottom = borderTop + jQuery(window).height();
	var borderLeft = jQuery(window).scrollLeft();
	var borderRight = borderLeft + jQuery(window).width();

	var width = this.outerWidth();
	var height = this.outerHeight();
	
	var left, top;
	var targetLeft, targetTop;
	var targetWidth, targetHeight;
	var targetX, targetY;

	if (alignment.target.element == undefined) { // align to mouse
		targetLeft = alignment.target.pageX;
		targetTop = alignment.target.pageY;
		targetWidth = 0;
		targetHeight = 0;
		targetX = 0;
		targetY = 0;
	} else {
		var target = $(alignment.target.element);
		targetLeft = target.offset().left;
		targetTop = target.offset().top;
		targetWidth = target.outerWidth();
		targetHeight = target.outerHeight();
		targetX = alignment.target.x;
		targetY = alignment.target.y;
	}		
	
	var anchor = targetWidth*targetX/100.0 + targetLeft;
	left = anchor - alignment.x*width/100.0;
	if (left < borderLeft || left + width > borderRight) {
		// flip horizontally to see if width can fit into screen
		anchor = targetWidth*(100-targetX)/100.0 + targetLeft;
		left = anchor - (100 - alignment.x) * width / 100.0;
		if (left < borderLeft || left + width > borderRight) {
			// does not fit even flipped, so we revert back
			anchor = targetWidth*targetX/100.0 + targetLeft;
			left = anchor - alignment.x*width/100.0;
		}
		if (left + width > borderRight)
			left = borderRight - width;
		if (left < borderLeft)
			left = borderLeft;
	}
	
	anchor = targetHeight * targetY / 100.0 + targetTop;
	top = anchor - alignment.y * height / 100.0;
	if (top < borderTop || top + height > borderBottom) {
		// flip vertically to see if height can fit into screen
		anchor = targetHeight * (100 - targetY) / 100.0 + targetTop;
		top = anchor - (100 - alignment.y) * height / 100.0;
		if (top < borderTop || top+height > borderBottom) {
			// does not fit even flipped, so we revert back
			anchor = targetHeight * targetY / 100.0 + targetTop;
			top = anchor - alignment.y * height / 100.0;
		}
		if (top + height > borderBottom)
			top = borderBottom - height;
		if (top < borderTop)
			top = borderTop;
	}

	if (left >= targetLeft + targetWidth) {
		if (left + width + alignment.offset <= borderRight)
			left += alignment.offset;
	} else if (left + width <= targetLeft) {
		if (left - alignment.offset >= borderLeft)
			left -= alignment.offset;
	} else if (top >= targetTop + targetHeight) {
		if (top + height + alignment.offset <= borderBottom) 
			top += alignment.offset;
	} else if (top + height <= targetTop) {
		if (top - alignment.offset >= borderTop) 
			top -= alignment.offset;
	}

	this.css({left:left, top:top});		
	return this;
};