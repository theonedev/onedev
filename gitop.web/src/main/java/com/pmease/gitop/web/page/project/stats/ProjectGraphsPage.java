package com.pmease.gitop.web.page.project.stats;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class ProjectGraphsPage extends ProjectCategoryPage {

	public ProjectGraphsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - graphs";
	}

}
