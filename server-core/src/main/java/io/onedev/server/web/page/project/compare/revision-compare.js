onedev.server.revisionCompare = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $revisionCompare = $("#revision-compare");
		function adjustHeight() {
			$revisionCompare.outerHeight($(window).height() - $revisionCompare.offset().top);
		}
		adjustHeight();
		$revisionCompare.on("resized", adjustHeight);
		$revisionCompare.scroll(function() {
			$revisionCompare.find(".scroll-aware").addBack(".scroll-aware").trigger("scrolled");
		});
	}
}