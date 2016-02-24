package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.entity.PullRequestActivity;

@SuppressWarnings("serial")
class ApprovePullRequest extends AbstractRenderableActivity {

	public ApprovePullRequest(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public Panel render(String panelId) {
		return new ApproveActivityPanel(panelId, this);
	}

}
