package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.RepositoryTabPage;

@SuppressWarnings("serial")
public class OpenRequestsPage extends RepositoryTabPage {

	public OpenRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(new RequestListPanel("content", true));
	}

}