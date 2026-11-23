onedev.server.pullRequestDetail = {
	onDomReady: function(scrollTopKey) {
		var scrollTop = localStorage.getItem(scrollTopKey);
		if (scrollTop) {
			$(".pull-request-detail").closest(".autofit").scrollTop(parseInt(scrollTop));
			localStorage.removeItem(scrollTopKey);
		}
		onedev.server.sideInfo.dockMoreInfoWithStickyTabs({
			root: ".pull-request-detail",
			stickySentinel: ".sticky-tabs-sentinel",
			stickyRow: ".sticky-tabs-row"
		});
	},
	onSummaryDomReady: function() {
		$(".pull-request-detail .card-body .main .more-info").click(function() {
			$(".pull-request-detail .card-header .side-info").click();
		});
	},
	markRebasedCommits: function(activitiesId, commitHashes, rebasedTooltip) {
		const currentCommits = new Set(commitHashes);
		const activities = document.getElementById(activitiesId);
		for (const row of activities.querySelectorAll(".commit[data-commit-hash]")) {
			const rebased = !currentCommits.has(row.dataset.commitHash);
			row.classList.toggle("rebased", rebased);
			if (rebased)
				row.title = rebasedTooltip;
			else
				row.removeAttribute("title");
		}
	}
}
