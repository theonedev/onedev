package com.pmease.gitplex.web.page.error;

import org.apache.commons.httpclient.HttpStatus;

public class AccessDeniedPage extends BaseErrorPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "403 - Access denied";
	}

	@Override
	protected int getErrorCode() {
		return HttpStatus.SC_FORBIDDEN;
	}

}
