onedev.server.requestDetail = {
	onSideDomReady: function(containerId) {
		var ps = new PerfectScrollbar(document.getElementById(containerId));
		$(window).resize(function() {
			ps.update();
		});
	}
}
