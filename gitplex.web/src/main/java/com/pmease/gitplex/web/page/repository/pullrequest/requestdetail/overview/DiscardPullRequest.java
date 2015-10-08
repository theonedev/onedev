package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.PullRequestActivity;

@SuppressWarnings("serial")
class DiscardPullRequest extends AbstractRenderableActivity {

	public DiscardPullRequest(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public Panel render(String panelId) {
		return new DiscardActivityPanel(panelId, this);
	}

}
