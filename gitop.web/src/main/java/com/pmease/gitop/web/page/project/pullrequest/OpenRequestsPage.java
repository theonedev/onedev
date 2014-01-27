package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class OpenRequestsPage extends ProjectCategoryPage {

	public OpenRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(new RequestListPanel("content", true));
	}

}