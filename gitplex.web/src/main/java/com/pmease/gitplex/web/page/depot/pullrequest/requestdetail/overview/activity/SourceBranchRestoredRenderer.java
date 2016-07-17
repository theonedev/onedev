package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import com.pmease.gitplex.core.entity.PullRequestActivity;

@SuppressWarnings("serial")
public class SourceBranchRestoredRenderer extends AbstractRenderer {

	public SourceBranchRestoredRenderer(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public ActivityPanel render(String panelId) {
		return new SourceBranchRestoredPanel(panelId, this);
	}

}
