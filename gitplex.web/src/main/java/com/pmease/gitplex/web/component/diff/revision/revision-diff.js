gitplex.revisionDiff = {
	init: function() {
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
		$diffStats.find("a.file").each(function() {
			var $this = $(this);
			var uri = URI(window.location.href);
			uri.removeSearch("mark-file").removeSearch("mark-pos");
			uri.removeSearch("jump-file").addSearch("jump-file", $this.data("file"));
			$this.attr("href", uri.toString());
			$this.click(function(e) {
				e.preventDefault();
				pmease.commons.history.pushState(uri.toString());
				gitplex.revisionDiff.jumpToFile($this.data("file"));
			});
		});
	},
	scroll: function() {
		var uri = URI(window.location.href); 
		var search = uri.search(true);
		if (search["jump-file"])
			gitplex.revisionDiff.jumpToFile(search["jump-file"]);
	},
	jumpToFile: function(file) {
		var $detail = $(".revision-diff>.body>.detail");
		var $fileDiff = $detail.find('li[data-file="' + file.escape() + '"]');
		$(window).scrollTop($fileDiff.offset().top);
		return false;
	}
}
