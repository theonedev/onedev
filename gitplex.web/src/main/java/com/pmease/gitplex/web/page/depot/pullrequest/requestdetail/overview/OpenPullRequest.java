package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.PullRequestActivity;

@SuppressWarnings("serial")
class OpenPullRequest extends AbstractRenderableActivity {

	public OpenPullRequest(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public Panel render(String panelId) {
		return new OpenActivityPanel(panelId, this);
	}

}
