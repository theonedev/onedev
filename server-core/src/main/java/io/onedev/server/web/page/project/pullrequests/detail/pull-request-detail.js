onedev.server.pullRequestDetail = {
	onDomReady: function(scrollTopKey) {
		var scrollTop = localStorage.getItem(scrollTopKey);
		if (scrollTop) {
			$(".pull-request-detail").closest(".autofit").scrollTop(parseInt(scrollTop));
			localStorage.removeItem(scrollTopKey);
		}
	},
	onSummaryDomReady: function() {
		$(".pull-request-detail .card-body .main .more-info").click(function() {
			$(".pull-request-detail .card-header .side-info").click();
		});
	}
}