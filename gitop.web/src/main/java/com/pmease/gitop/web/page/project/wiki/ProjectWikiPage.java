package com.pmease.gitop.web.page.project.wiki;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class ProjectWikiPage extends ProjectCategoryPage {

	public ProjectWikiPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - wiki";
	}

}
