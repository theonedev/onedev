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
		}
    	
    	return this;
    };
 
}( jQuery ));
