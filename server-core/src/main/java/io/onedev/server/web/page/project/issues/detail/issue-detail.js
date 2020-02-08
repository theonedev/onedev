onedev.server.issueDetail = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $main = $("#issue-detail>.main");
		function adjustHeight() {
			$main.outerHeight($(window).height() - $main.offset().top);
		}
		adjustHeight();
		$main.addClass("resize-aware").on("resized", adjustHeight);
	}
}