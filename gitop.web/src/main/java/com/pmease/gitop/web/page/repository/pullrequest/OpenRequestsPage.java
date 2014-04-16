package com.pmease.gitop.web.page.repository.pullrequest;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.repository.RepositoryTabPage;

@SuppressWarnings("serial")
public class OpenRequestsPage extends RepositoryTabPage {

	public OpenRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new RequestListPanel("content", true));
	}

}