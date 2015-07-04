gitplex.editSave = {
	init: function(containerId) {
		var $editSave = $("#" + containerId + ">.edit-save");
		$editSave.scroll(function() {
	    	pmease.commons.history.setScrollPos({left: $body.scrollLeft(), top: $body.scrollTop()});
		});

		gitplex.expandable.getScrollTop = function() {
			return $editSave.scrollTop();			
		};
		gitplex.expandable.setScrollTop = function(scrollTop) {
			$editSave.scrollTop(scrollTop);
		};

		$editSave.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$editSave.outerWidth(width);
			$editSave.outerHeight(height);
			
			var scrollPos = pmease.commons.history.getScrollPos();
			if (scrollPos) {
				$editSave.scrollLeft(scrollPos.left);
				$editSave.scrollTop(scrollPos.top);
			}
		});
		
		$(window).resize();
	}
}
