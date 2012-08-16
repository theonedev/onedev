jQuery.fn.align = function(alignment) {
	var borderTop = jQuery(window).scrollTop();
	var borderBottom = borderTop + jQuery(window).height();
	var borderLeft = jQuery(window).scrollLeft();
	var borderRight = borderLeft + jQuery(window).width();
	var width = this.outerWidth();
	var height = this.outerHeight();
	var left, top;
	if (alignment.pageX) { // alignment is mouse
		var offset = 4;
		if (borderRight - offset >= width + alignment.pageX)
			left = alignment.pageX + offset;
		else
			left = borderRight - width - offset;

		if(borderTop + offset >= alignment.pageY - height)
			top = borderTop + offset;
		 else
			top = alignment.pageY - height - offset;
	} else {
		var target = $(alignment.target);
		var targetLeft = target.offset().left;
		var targetTop = target.offset().top;
		var targetWidth = target.outerWidth();
		var targetHeight = target.outerHeight();
		var anchor = targetWidth*alignment.targetX/100.0 + targetLeft;
		left = anchor - alignment.dropdownX*width/100.0;
		if (left<borderLeft || left+width>borderRight) {
			anchor = targetWidth*(100-alignment.targetX)/100.0 + targetLeft;
			left = anchor - (100-alignment.dropdownX)*width/100.0;
			if (left<borderLeft)
				left = borderLeft;
			if (left+width > borderRight)
				left = borderRight-width;
		}
		
		anchor = targetHeight*alignment.targetY/100.0 + targetTop;
		top = anchor - alignment.dropdownY*height/100.0;
		if (top<borderTop || top+height>borderBottom) {
			anchor = targetHeight*(100-alignment.targetY)/100.0 + targetTop;
			top = anchor - (100-alignment.dropdownY)*height/100.0;
			if (top<borderTop)
				top = borderTop;
			if (top+height > borderBottom)
				top = borderBottom-height;
		}

		this.removeClass("align-left align-right align-top align-bottom align-overlap");
		if (left>=targetLeft+targetWidth) {
			this.addClass("align-right");
			var targetCenter = targetTop + targetHeight/2.0;
			this.css({backgroundPosition:["0% " + (targetCenter-top)/height*100 + "%"]});
		} else if (left+width<=targetLeft) {
			this.addClass("align-left");
			var targetCenter = targetTop + targetHeight/2.0;
			this.css({backgroundPosition:["0% " + (targetCenter-top)/height*100 + "%"]});
		} else if (top>=targetTop+targetHeight) {
			this.addClass("align-bottom");
			var targetCenter = targetLeft + targetWidth/2.0;
			this.css({backgroundPosition:[(targetCenter-left)/width*100 + "% 0%"]});
		} else if (top+height<=targetTop) {
			this.addClass("align-top");
			var targetCenter = targetLeft + targetWidth/2.0;
			this.css({backgroundPosition:[(targetCenter-left)/width*100 + "% 0%"]});
		} else {
			this.addClass("align-overlap");
		}
	}
	this.css({left:left, top:top});		
	return this;
};
