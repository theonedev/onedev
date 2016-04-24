gitplex.revisionDiff = {
	init: function() {
		var cookieName = "revisionDiff.showDiffStats";
		var $container = $(".revision-diff");
		var $diffStats = $container.find(".diff-stats");
		var $diffStatsToggle = $container.find("a.total-changed");
		$diffStatsToggle.click(function() {
			if ($diffStats.is(":visible")) {
				$diffStats.hide();
				$diffStatsToggle.removeClass("diff-stats");
				Cookies.set(cookieName, "no", {expires: Infinity});
			} else {
				$diffStats.show();
				$diffStatsToggle.addClass("diff-stats");
				Cookies.set(cookieName, "yes", {expires: Infinity});
			}
			$(document.body).trigger('sticky_kit:recalc');		
		});
		return false;
		
	},
	jumpToFile: function(file, url) {
		if (url) 
			history.pushState(undefined, '', url);
		var $container = $(".revision-diff");
		var $fileDiff = $container.find('*[data-file="' + file.escape() + '"]');
		$(window).scrollTop($fileDiff.offset().top - $(".sticky").outerHeight());
		return false;
	}
}

$(function() {
	var uri = URI(window.location.href); 
	var fragment = uri.fragment(true);
	if (fragment.file) {
		gitplex.revisionDiff.jumpToFile(fragment.file);	
	}
});
