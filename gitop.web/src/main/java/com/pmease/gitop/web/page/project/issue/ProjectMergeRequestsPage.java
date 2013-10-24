package com.pmease.gitop.web.page.project.issue;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class ProjectMergeRequestsPage extends ProjectCategoryPage {

	public ProjectMergeRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.MERGE_REQUESTS;
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - merge requests";
	}

}
