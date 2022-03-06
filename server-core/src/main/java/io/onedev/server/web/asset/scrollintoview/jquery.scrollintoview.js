/*
 * 2015, Robin Shen
 */
(function ( $ ) {
 
    $.fn.scrollIntoView = function(scrollParent, animation) {
    	var $this = jQuery(this);

		var $scrollParent = scrollParent? $(scrollParent): $(this).scrollParent(true);
	
		if ($this.length != 0) {
			var scrollParentTop, scrollParentBottom, thisTop, thisBottom;
			if ($scrollParent[0] == document) {
				scrollParentTop = 0;
				scrollParentBottom = $(window).height();
				thisTop = $this.offset().top - $(window).scrollTop();
			} else {
				scrollParentTop = $scrollParent.offset().top;
				scrollParentBottom = scrollParentTop + $scrollParent.height();
				thisTop = $this.offset().top;
			}		
			var thisHeight = $this.outerHeight();
			thisBottom = thisTop + thisHeight;

			if (thisHeight > scrollParentBottom - scrollParentTop) {
				var scrollOffset = Math.floor(thisTop - scrollParentTop);
				if (animation)
					$scrollParent.animate({scrollTop: $scrollParent.scrollTop() + scrollOffset});
				else
					$scrollParent.scrollTop($scrollParent.scrollTop() + scrollOffset);
			} else {
				var beyondTop = scrollParentTop - thisTop;
				if (beyondTop > 0) {
					beyondTop = Math.ceil(beyondTop);
					if (animation)
						$scrollParent.animate({scrollTop: $scrollParent.scrollTop() - beyondTop});
					else
						$scrollParent.scrollTop($scrollParent.scrollTop() - beyondTop);
				} else {
					var beyondBottom = thisBottom - scrollParentBottom;
					if (beyondBottom > 0) {
						beyondBottom = Math.ceil(beyondBottom);
						if (animation)
							$scrollParent.animate({scrollTop: $scrollParent.scrollTop() + beyondBottom});
						else
							$scrollParent.scrollTop($scrollParent.scrollTop() + beyondBottom);
					} 
				}
			}
			
			var scrollParentLeft, scrollParentRight, thisLeft, thisRight;
			if ($scrollParent[0] == document) {
				scrollParentLeft = 0;
				scrollParentRight = $(window).width();
				thisLeft = $this.offset().left - $(window).scrollLeft();
			} else {
				scrollParentLeft = $scrollParent.offset().left;
				scrollParentRight = scrollParentLeft + $scrollParent.width();
				thisLeft = $this.offset().left;
			}		
			var thisWidth = $this.outerWidth();
			thisRight = thisLeft + thisWidth;

			if (thisWidth > scrollParentRight - scrollParentLeft) {
				var scrollOffset = Math.floor(thisLeft - scrollParentLeft);
				if (animation)
					$scrollParent.animate({scrollLeft: $scrollParent.scrollLeft() + scrollOffset});
				else
					$scrollParent.scrollLeft($scrollParent.scrollLeft() + scrollOffset);
			} else {
				var beyondLeft = scrollParentLeft - thisLeft;
				if (beyondLeft > 0) {
					beyondLeft = Math.ceil(beyondLeft);
					if (animation)
						$scrollParent.animate({scrollLeft: $scrollParent.scrollLeft() - beyondLeft});
					else
						$scrollParent.scrollLeft($scrollParent.scrollLeft() - beyondLeft);
				} else {
					var beyondRight = thisRight - scrollParentRight;
					if (beyondRight > 0) {
						beyondRight = Math.ceil(beyondRight);
						if (animation)
							$scrollParent.animate({scrollLeft: $scrollParent.scrollLeft() + beyondRight});
						else
							$scrollParent.scrollLeft($scrollParent.scrollLeft() + beyondRight);
					} 
				}
			}
		}
    	
    	return this;
    };
 
}( jQuery ));
