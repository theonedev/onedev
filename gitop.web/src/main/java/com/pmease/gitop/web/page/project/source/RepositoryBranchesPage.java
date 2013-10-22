package com.pmease.gitop.web.page.project.source;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class RepositoryBranchesPage extends ProjectCategoryPage {

	public RepositoryBranchesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.BRANCHES;
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - branches";
	}

}
