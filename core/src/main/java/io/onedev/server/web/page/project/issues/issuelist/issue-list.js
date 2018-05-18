onedev.server.issueList = {
	onDomReady: function() {
		var ps = new PerfectScrollbar($("#issue-list>.side")[0]);
		$(window).resize(function() {
			ps.update();
		});
	}
}
