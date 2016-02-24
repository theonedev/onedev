package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.entity.PullRequestActivity;

@SuppressWarnings("serial")
class RestoreSourceBranch extends AbstractRenderableActivity {

	public RestoreSourceBranch(PullRequestActivity activity) {
		super(activity);
	}
	
	@Override
	public Panel render(String panelId) {
		return new RestoreSourceBranchActivityPanel(panelId, this);
	}

}
