jQuery.fn.align = function(alignment) {
	if (this[0] == undefined)
		return this;
	
	if (alignment == undefined)
		alignment = this[0].alignment;
	
	var indicator = this.find(">.indicator");
	
	var borderTop = jQuery(window).scrollTop();
	var borderBottom = borderTop + jQuery(window).height();
	var borderLeft = jQuery(window).scrollLeft();
	var borderRight = borderLeft + jQuery(window).width();

	var indicatorSize = 6;

	var width = this.outerWidth();
	var height = this.outerHeight();
	
	var left, top;
	if (alignment.pageX != undefined) { // align to mouse
		if (alignment.pageX - width/2 >= borderLeft) {
			if (alignment.pageX + width/2 <= borderRight)
				left = alignment.pageX - width/2;
			else
				left = borderRight - width;
		} else {
			left = borderLeft;
		} 
		var offset = 2;
		
		if (indicator[0] && indicator[1]) {
			var indicatorLeft = alignment.pageX - left - indicatorSize;
			if (indicatorLeft < indicatorSize)
				indicatorLeft = indicatorSize;
			if (indicatorLeft > width - 3*indicatorSize)
				indicatorLeft = width - 3*indicatorSize;
			
			$(indicator[0]).css({left: (indicatorLeft - 1) + "px"});
			$(indicator[1]).css({left: indicatorLeft + "px"});
			
			indicator.removeClass("top");
			indicator.removeClass("bottom");
			
			if (borderTop + offset + indicatorSize < alignment.pageY - height) {
				top = alignment.pageY - height - offset - indicatorSize;
				indicator.addClass("top");
			} else if (alignment.pageY + offset * 8 + indicatorSize + height <= borderBottom){
				top = alignment.pageY + offset * 8 + indicatorSize;
				indicator.addClass("bottom");
			} else {
				top = borderTop;
			}
		} else {
			if (borderTop + offset < alignment.pageY - height) 
				top = alignment.pageY - height - offset;
			else if (alignment.pageY + offset * 8 + height <= borderBottom)
				top = alignment.pageY + offset * 8;
			else 
				top = borderTop;
		}
	} else {
		var target = $(alignment.target);
		var targetLeft = target.offset().left;
		var targetTop = target.offset().top;
		var targetWidth = target.outerWidth();
		var targetHeight = target.outerHeight();
		var anchor = targetWidth*alignment.targetX/100.0 + targetLeft;
		left = anchor - alignment.dropdownX*width/100.0;
		if (left < borderLeft || left + width > borderRight) {
			anchor = targetWidth*(100-alignment.targetX)/100.0 + targetLeft;
			left = anchor - (100 - alignment.dropdownX) * width / 100.0;
			if (left < borderLeft)
				left = borderLeft;
			if (left + width > borderRight)
				left = borderRight - width;
		}
		
		anchor = targetHeight * alignment.targetY / 100.0 + targetTop;
		top = anchor - alignment.dropdownY * height / 100.0;
		if (top < borderTop || top + height > borderBottom) {
			anchor = targetHeight * (100 - alignment.targetY) / 100.0 + targetTop;
			top = anchor - (100 - alignment.dropdownY) * height / 100.0;
			if (top<borderTop)
				top = borderTop;
			if (top+height > borderBottom)
				top = borderBottom - height;
		}

		if (indicator[0] && indicator[1]) {
			indicator.removeClass("left right top bottom");
			if (left >= targetLeft + targetWidth) {
				if (left + width + alignment.gap <= borderRight) {
					indicator.addClass("right");
					left += alignment.gap;
					var targetCenter = targetTop + targetHeight/2.0;
					var indicatorTop = targetCenter - top - indicatorSize;
					if (indicatorTop < indicatorSize)
						indicatorTop = indicatorSize;
					if (indicatorTop > height - 3 * indicatorSize)
						indicatorTop = height - 3 * indicatorSize;
					$(indicator[0]).css({top: (indicatorTop - 1) + "px"});
					$(indicator[1]).css({top: indicatorTop + "px"});
				}
			} else if (left + width <= targetLeft) {
				if (left - alignment.gap >= borderLeft) {
					indicator.addClass("left");
					left -= alignment.gap;
					var targetCenter = targetTop + targetHeight/2.0;
					var indicatorTop = targetCenter - top - indicatorSize;
					if (indicatorTop < indicatorSize)
						indicatorTop = indicatorSize;
					if (indicatorTop > height - 3 * indicatorSize)
						indicatorTop = height - 3 * indicatorSize;
					$(indicator[0]).css({top: (indicatorTop - 1) + "px"});
					$(indicator[1]).css({top: indicatorTop + "px"});
				}
			} else if (top >= targetTop + targetHeight) {
				if (top + height + alignment.gap <= borderBottom) {
					indicator.addClass("bottom");
					top += alignment.gap;
					var targetCenter = targetLeft + targetWidth/2.0;
					var indicatorLeft = targetCenter - left - indicatorSize;
					if (indicatorLeft < indicatorSize)
						indicatorLeft = indicatorSize;
					if (indicatorLeft > width - 3 * indicatorSize)
						indicatorLeft = width - 3 * indicatorSize;
					$(indicator[0]).css({left: (indicatorLeft - 1) + "px"});
					$(indicator[1]).css({left: indicatorLeft + "px"});
				}
			} else if (top + height <= targetTop) {
				if (top - alignment.gap >= borderTop) {
					indicator.addClass("top");
					top -= alignment.gap;
					var targetCenter = targetLeft + targetWidth/2.0;
					var indicatorLeft = targetCenter - left - indicatorSize;
					if (indicatorLeft < indicatorSize)
						indicatorLeft = indicatorSize;
					if (indicatorLeft > width - 3 * indicatorSize)
						indicatorLeft = width - 3 * indicatorSize;
					$(indicator[0]).css({left: (indicatorLeft - 1) + "px"});
					$(indicator[1]).css({left: indicatorLeft + "px"});
				}
			}
		} else {
			if (left >= targetLeft + targetWidth) {
				if (left + width + alignment.gap <= borderRight)
					left += alignment.gap;
			} else if (left + width <= targetLeft) {
				if (left - alignment.gap >= borderLeft)
					left -= alignment.gap;
			} else if (top >= targetTop + targetHeight) {
				if (top + height + alignment.gap <= borderBottom) 
					top += alignment.gap;
			} else if (top + height <= targetTop) {
				if (top - alignment.gap >= borderTop) 
					top -= alignment.gap;
			}
		}
	}
	this.css({left:left, top:top});		
	return this;
};
