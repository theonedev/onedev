package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import com.pmease.gitplex.core.entity.PullRequest;

@SuppressWarnings("serial")
public class OpenedRenderer extends AbstractRenderer {

	public OpenedRenderer(PullRequest request) {
		super(request, request.getSubmitter(), request.getSubmitDate());
	}
	
	@Override
	public ActivityPanel render(String panelId) {
		return new OpenedPanel(panelId, this);
	}

}
