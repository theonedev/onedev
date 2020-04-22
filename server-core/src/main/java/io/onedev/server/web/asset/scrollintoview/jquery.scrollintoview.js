/*
 * 2015, Robin Shen
 */
(function ( $ ) {
 
	var defaultMargin = 8;
	
    $.fn.scrollIntoView = function(margin) {
    	if (margin == undefined)
    		margin = defaultMargin;
    	
    	var $this = jQuery(this);
		var $scrollParent = $this.scrollParent();
		
		if ($this.length != 0) {
			var scrollParentTop, scrollParentBottom, thisTop, thisBottom;
			if ($scrollParent[0] == document) {
				scrollParentTop = 0;
				scrollParentBottom = $(window).height();
				thisTop = $this.offset().top - $(window).scrollTop();
				thisBottom = thisTop + $this.height();
			} else {
				scrollParentTop = $scrollParent.offset().top;
				scrollParentBottom = scrollParentTop + $scrollParent.height();
				thisTop = $this.offset().top;
				thisBottom = $this.offset().top + $this.height();
			}		
			
			if (thisBottom + margin > scrollParentBottom)
				$scrollParent.scrollTop($scrollParent.scrollTop() + (thisBottom + margin - scrollParentBottom));

			if (thisTop - margin < scrollParentTop)
				$scrollParent.scrollTop($scrollParent.scrollTop() - (scrollParentTop - thisTop + margin));
		}
    	
    	return this;
    };
 
}( jQuery ));
