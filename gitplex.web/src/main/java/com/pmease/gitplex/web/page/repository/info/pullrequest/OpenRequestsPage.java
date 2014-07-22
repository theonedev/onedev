package com.pmease.gitplex.web.page.repository.info.pullrequest;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;

@SuppressWarnings("serial")
public class OpenRequestsPage extends RepositoryInfoPage {

	public OpenRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new RequestListPanel("content", true));
	}

}