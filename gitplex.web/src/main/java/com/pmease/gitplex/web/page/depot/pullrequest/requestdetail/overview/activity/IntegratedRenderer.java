package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import com.pmease.gitplex.core.entity.PullRequestActivity;

@SuppressWarnings("serial")
public class IntegratedRenderer extends AbstractRenderer {

	public IntegratedRenderer(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public ActivityPanel render(String panelId) {
		return new IntegratedPanel(panelId, this);
	}

}
