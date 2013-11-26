package com.pmease.gitop.web.page.project.issue;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class ProjectPullRequestsPage extends ProjectCategoryPage {

	public ProjectPullRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.PULL_REQUESTS;
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - pull requests";
	}

}
