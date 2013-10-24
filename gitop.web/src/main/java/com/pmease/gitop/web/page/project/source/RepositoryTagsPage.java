package com.pmease.gitop.web.page.project.source;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class RepositoryTagsPage extends ProjectCategoryPage {

	public RepositoryTagsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.TAGS;
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - tags";
	}

}
