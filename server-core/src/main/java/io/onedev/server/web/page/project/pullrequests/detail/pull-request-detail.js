onedev.server.pullRequestDetail = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $main = $("#request-detail>.main");
		function adjustHeight() {
			$main.outerHeight($(window).height() - $main.offset().top);
		}
		adjustHeight();
		$main.on("resized", adjustHeight);
		$main.scroll(function() {
			$main.find(".scroll-aware").addBack(".scroll-aware").trigger("scrolled");
		});
	}
}