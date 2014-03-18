package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class RepositoryHooksPage extends AbstractRepositorySettingPage {

	public RepositoryHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Hooks - " + getProject();
	}

}
