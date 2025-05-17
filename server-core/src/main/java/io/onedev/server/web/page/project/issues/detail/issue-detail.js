onedev.server.issueDetail = {
	onDomReady: function(scrollTopKey) {
		var scrollTop = localStorage.getItem(scrollTopKey);
		if (scrollTop) {
			$(".issue-detail").closest(".autofit").scrollTop(parseInt(scrollTop));
			localStorage.removeItem(scrollTopKey);
		}
	}
};