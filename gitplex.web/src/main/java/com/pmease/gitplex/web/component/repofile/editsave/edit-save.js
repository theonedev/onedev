gitplex.editsave = {
	init: function(containerId) {
		var $editSave = $("#" + containerId + ">.edit-save");
		$editSave.scroll(function() {
	    	pmease.commons.history.setScrollPos({left: $editSave.scrollLeft(), top: $editSave.scrollTop()});
		});

		gitplex.expandable.getScrollTop = function() {
			return $editSave.scrollTop();			
		};
		gitplex.expandable.setScrollTop = function(scrollTop) {
			$editSave.scrollTop(scrollTop);
		};

		$editSave.on("autofit", function(event, width, height) {
			event.stopPropagation();
			
			$editSave.closest(".body").css("overflow", "hidden");
			$editSave.outerWidth(width);
			$editSave.outerHeight(height);
			
			var scrollPos = pmease.commons.history.getScrollPos();
			if (scrollPos) {
				$editSave.scrollLeft(scrollPos.left);
				$editSave.scrollTop(scrollPos.top);
			}
		});
		
		$editSave.on("contentEdit", function(event, contentChanged) {
			event.stopPropagation();
			$editSave.data("contentChanged", contentChanged);
			gitplex.editsave.updateSubmitBtnState($editSave);
		});
	}, 
	
	updateSubmitBtnState: function($editSave) {
		var $submitBtn = $editSave.find("input[type=submit]");
		if ($editSave.data("contentChanged") === true || $editSave.data("pathChanged") === true)
			$submitBtn.removeAttr("disabled");
		else
			$submitBtn.attr("disabled", "disabled");
	},
	
	onPathChange: function(containerId, defaultCommitMessage, equalsOldPath) {
		var $editSave = $("#" + containerId + ">.edit-save");
		$editSave.find(".summary-commit-message").attr("placeholder", defaultCommitMessage);
		$editSave.data("pathChanged", !equalsOldPath);
		gitplex.editsave.updateSubmitBtnState($editSave);
	}
}
