gitplex.saveChange = {
	init: function(containerId) {
		var $saveChange = $("#" + containerId + ">.save-change");
		$saveChange.scroll(function() {
	    	pmease.commons.history.setScrollPos({left: $body.scrollLeft(), top: $body.scrollTop()});
		});

		gitplex.expandable.getScrollTop = function() {
			return $saveChange.scrollTop();			
		};
		gitplex.expandable.setScrollTop = function(scrollTop) {
			$saveChange.scrollTop(scrollTop);
		};

		$saveChange.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$saveChange.outerWidth(width);
			$saveChange.outerHeight(height);
			
			var scrollPos = pmease.commons.history.getScrollPos();
			if (scrollPos) {
				$saveChange.scrollLeft(scrollPos.left);
				$saveChange.scrollTop(scrollPos.top);
			}
		});
		
		$(window).resize();
	}
}
