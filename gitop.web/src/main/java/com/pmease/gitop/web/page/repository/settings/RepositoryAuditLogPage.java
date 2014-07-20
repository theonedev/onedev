package com.pmease.gitop.web.page.repository.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class RepositoryAuditLogPage extends RepositoryAdministrationPage {

	public RepositoryAuditLogPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Audit Log - " + getRepository();
	}
}
