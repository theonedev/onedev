onedev.server.commitDetail = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $commitDetail = $("#commit-detail");
		function adjustHeight() {
			$commitDetail.outerHeight($(window).height() - $commitDetail.offset().top);
		}
		adjustHeight();
		$commitDetail.on("resized", adjustHeight);
		$commitDetail.scroll(function() {
			$commitDetail.find(".scroll-aware").addBack(".scroll-aware").trigger("scrolled");
		});
	},
	initRefs: function(refsId) {
		var $refs = $("#" + refsId);
		if ($refs.children().length != 0)
			$refs.oneline("<a class='ellipsis-expander'><i class='fa fa-ellipsis-h'></i></a>", 10, 120);
		else 
			$refs.parent().remove();
	}
};