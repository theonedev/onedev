package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.entity.PullRequestActivity;

@SuppressWarnings("serial")
class ReopenPullRequest extends AbstractRenderableActivity {

	public ReopenPullRequest(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public Panel render(String panelId) {
		return new ReopenActivityPanel(panelId, this);
	}

}
