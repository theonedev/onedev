gitplex.requestChanges = {
	initCommitSelector: function(fromIndex, toIndex) {
		var $commits = $(".commit-selector ul a");
		if (fromIndex!=undefined && toIndex!=undefined) {
			$commits.slice(fromIndex, toIndex).addClass("selected");
		}
	}
}