onedev.server.issueDetail = {
	onDomReady: function() {
		var ps = new PerfectScrollbar($("#issue-detail>.side")[0]);
		$(window).resize(function() {
			ps.update();
		});
	}
}
