onedev.server.requestChanges = {
	initCommitSelector: function(callback, baseCommit, fromIndex, toIndex) {
		var $commits = $(".commits-selector ul a");
		if (fromIndex!=undefined && toIndex!=undefined) {
			$commits.slice(fromIndex, toIndex+1).addClass("selected");
		}
		var fromIndexToApply = fromIndex;
		var toIndexToApply = toIndex;
		
		function onIndexChanged() {
			var $apply = $(".commits-selector>.links a.selected-changes"); 
			if (fromIndexToApply != fromIndex || toIndexToApply != toIndex) {
				$apply.removeClass("disabled").off("click").click(function() {
					var oldCommit;
					if (fromIndexToApply == 0)
						oldCommit = baseCommit;
					else
						oldCommit = $($(".commits-selector ul li").get(fromIndexToApply-1)).data("hash");
					var newCommit = $($(".commits-selector ul li").get(toIndexToApply)).data("hash");
					callback(oldCommit, newCommit);
				});
			} else {
				$apply.addClass("disabled");
			}
			$commits.removeClass("selected").slice(fromIndexToApply, toIndexToApply+1).addClass("selected");
		}
		
		$commits.click(function(e) {
			var index = $commits.index(this);
			if (e.shiftKey) {
				if (fromIndexToApply != undefined && toIndexToApply != undefined) {
					if (index < fromIndexToApply) {
						fromIndexToApply = index;
					} else if (index > toIndexToApply) {
						toIndexToApply = index;
					} 
				} else {
					fromIndexToApply = toIndexToApply = index;
				}
			} else {
				fromIndexToApply = toIndexToApply = index;
			}
			onIndexChanged();
		});
	}
};