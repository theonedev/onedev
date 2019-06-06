onedev.server.imageDiff = {
	onDomReady: function(containerId) {
		$("#" + containerId + " img").on("load", function() {
//			$(window).resize();
		});
	}
}