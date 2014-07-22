package com.pmease.gitplex.web.page.repository.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class RepoAuditPage extends RepoAdminPage {

	public RepoAuditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Audit Log - " + getRepository();
	}
}
