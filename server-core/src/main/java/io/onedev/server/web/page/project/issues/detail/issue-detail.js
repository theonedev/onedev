onedev.server.issueDetail = {
	onDomReady: function(scrollTopKey) {
		var scrollTop = localStorage.getItem(scrollTopKey);
		if (scrollTop) {
			$(".issue-detail").closest(".autofit").scrollTop(parseInt(scrollTop));
			localStorage.removeItem(scrollTopKey);
		}
		onedev.server.sideInfo.dockMoreInfoWithStickyTabs({
			root: ".issue-detail",
			stickySentinel: ".sticky-tabs-sentinel",
			stickyRow: ".sticky-tabs-row"
		});
	}
};
