onedev.server.revisionDiff = {
	onDomReady: function() {
		var cookieName = "revisionDiff.showDiffStats";
		var $body = $(".revision-diff>.body");
		var $diffStats = $body.children(".diff-stats");
		var $diffStatsToggle = $body.find(">.title a");
		$diffStatsToggle.click(function() {
			if ($diffStats.is(":visible")) {
				$diffStats.hide();
				$diffStatsToggle.removeClass("expanded");
				Cookies.set(cookieName, "no", {expires: Infinity});
			} else {
				$diffStats.show();
				$diffStatsToggle.addClass("expanded");
				Cookies.set(cookieName, "yes", {expires: Infinity});
			}
		});
	},
	initComment: function() {
		var $comment = $(".revision-diff>.body>.detail>.comment");
		
		if ($comment.is(":visible")) {
			var $commentResizeHandle = $comment.children(".ui-resizable-handle");
			$comment.resizable({
				autoHide: false,
				handles: {"e": $commentResizeHandle},
				minWidth: 200,
				stop: function(e, ui) {
					Cookies.set("revisionDiff.comment.width", ui.size.width, {expires: Infinity});
				}
			});
		}
	}
};
