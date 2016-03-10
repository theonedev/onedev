gitplex.editsave = {
	init: function(containerId) {
		var $editSave = $("#" + containerId + ">.edit-save");
		$editSave.scroll(function() {
	    	pmease.commons.history.setViewState({scroll:{left: $editSave.scrollLeft(), top: $editSave.scrollTop()}});
		});

		$editSave.on("autofit", function(event, width, height) {
			event.stopPropagation();
			
			$editSave.closest(".body").css("overflow", "hidden");
			$editSave.outerWidth(width);
			$editSave.outerHeight(height);
			
			var viewState = pmease.commons.history.getViewState();
			if (viewState) {
				$editSave.scrollLeft(viewState.scroll.left);
				$editSave.scrollTop(viewState.scroll.top);
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
