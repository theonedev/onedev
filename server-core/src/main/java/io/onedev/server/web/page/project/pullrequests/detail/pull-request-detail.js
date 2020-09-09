onedev.server.pullRequestDetail = {
	onDomReady: function() {
		var $main = $(".pull-request-detail>.card-body>.main");
		$main.find(">.summary .more-info").click(function() {
			$main.find(">.status-and-branches .side-info").click();
		});
	}
}