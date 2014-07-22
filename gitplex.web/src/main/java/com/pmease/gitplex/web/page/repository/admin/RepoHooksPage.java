package com.pmease.gitplex.web.page.repository.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class RepoHooksPage extends RepoAdminPage {

	public RepoHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Hooks - " + getRepository();
	}

}
