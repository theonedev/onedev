package com.pmease.gitop.web.page.project;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class ProjectHomePage extends AbstractProjectPage {

	@Override
	protected String getPageTitle() {
		return getAccount().getName() + "/" + getProject().getName();
	}

	public ProjectHomePage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
	}
}
