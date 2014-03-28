package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.RepositoryTabPage;

@SuppressWarnings("serial")
public class ClosedRequestsPage extends RepositoryTabPage {

	public ClosedRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new RequestListPanel("content", false));
	}

}