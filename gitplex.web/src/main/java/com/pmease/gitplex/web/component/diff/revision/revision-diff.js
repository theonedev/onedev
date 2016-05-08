gitplex.revisionDiff = {
	init: function() {
		var cookieName = "revisionDiff.showDiffStats";
		var $container = $(".revision-diff");
		var $diffStats = $container.find("ul.diff-stats");
		var $diffStatsToggle = $container.find("a.total-changed");
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
			$(document.body).trigger('sticky_kit:recalc');		
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
		var $container = $(".revision-diff");
		var $fileDiff = $container.find('li[data-file="' + file.escape() + '"]');
		$(window).scrollTop($fileDiff.offset().top);
		return false;
	}
}
