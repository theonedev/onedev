gitplex.server.editsave = {
	init: function(containerId) {
		var $editSave = $("#" + containerId + ">.edit-save");
		$editSave.on("storeViewState", function(e) {
			e.stopPropagation();
	    	gitplex.commons.history.setViewState({scroll:{left: $editSave.scrollLeft(), top: $editSave.scrollTop()}});
		});
		$editSave.on("autofit", function(e, width, height) {
			e.stopPropagation();
			
			$editSave.closest(".body").css("overflow", "hidden");
			$editSave.outerWidth(width);
			$editSave.outerHeight(height);
			
			var viewState = gitplex.commons.history.getViewState();
			if (viewState && viewState.scroll) {
				$editSave.scrollLeft(viewState.scroll.left);
				$editSave.scrollTop(viewState.scroll.top);
			}
		});
		
		$editSave.on("contentEdit", function(event, contentChanged) {
			event.stopPropagation();
			$editSave.data("contentChanged", contentChanged);
			gitplex.server.editsave.updateSubmitBtnState($editSave);
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
		gitplex.server.editsave.updateSubmitBtnState($editSave);
	}
};
