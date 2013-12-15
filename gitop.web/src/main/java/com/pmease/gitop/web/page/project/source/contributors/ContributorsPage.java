package com.pmease.gitop.web.page.project.source.contributors;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class ContributorsPage extends ProjectCategoryPage {

	public ContributorsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.CONTRIBUTORS;
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - contributors";
	}

}
