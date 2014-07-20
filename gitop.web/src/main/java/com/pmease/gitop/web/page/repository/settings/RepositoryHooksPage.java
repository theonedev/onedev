package com.pmease.gitop.web.page.repository.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class RepositoryHooksPage extends RepositoryAdministrationPage {

	public RepositoryHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Hooks - " + getRepository();
	}

}
