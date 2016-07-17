package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import com.pmease.gitplex.core.entity.PullRequestActivity;

@SuppressWarnings("serial")
public class ReviewWithdrawedRenderer extends AbstractRenderer {

	public ReviewWithdrawedRenderer(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public ActivityPanel render(String panelId) {
		return new ReviewWithdrawedPanel(panelId, this);
	}

}
