onedev.server.pullRequestDetail = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $main = $("#request-detail>.main");
		function adjustHeight() {
			$main.outerHeight($(window).height() - $main.offset().top);
		}
		adjustHeight();
		$main.addClass("resize-aware").on("resized", adjustHeight);
	}
}