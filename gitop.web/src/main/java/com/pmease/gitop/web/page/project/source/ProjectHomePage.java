package com.pmease.gitop.web.page.project.source;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class ProjectHomePage extends SourceTreePage {

	public ProjectHomePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getAccount().getName() + "/" + getProject().getName();
	}
}
