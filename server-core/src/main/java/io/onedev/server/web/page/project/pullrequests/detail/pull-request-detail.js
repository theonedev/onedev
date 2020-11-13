onedev.server.pullRequestDetail = {
	onSummaryDomReady: function() {
		$(".pull-request-detail .card-body .main .more-info").click(function() {
			$(".pull-request-detail .card-header .side-info").click();
		});
	}
}