onedev.server.newPullRequest = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $newPullRequest = $("#new-pull-request");
		function adjustHeight() {
			$newPullRequest.outerHeight($(window).height() - $newPullRequest.offset().top);
		}
		adjustHeight();
		$newPullRequest.on("resized", adjustHeight);
		$newPullRequest.scroll(function() {
			$newPullRequest.find(".scroll-aware").addBack(".scroll-aware").trigger("scrolled");
		});
	}
}