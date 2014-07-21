package com.pmease.gitplex.web.page.error;

import org.apache.commons.httpclient.HttpStatus;

public class InternalServerErrorPage extends BaseErrorPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "500 - Internal Error";
	}

	@Override
	protected int getErrorCode() {
		return HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

}
