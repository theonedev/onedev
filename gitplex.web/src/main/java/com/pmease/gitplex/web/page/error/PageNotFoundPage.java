package com.pmease.gitplex.web.page.error;

import org.apache.commons.httpclient.HttpStatus;

@SuppressWarnings("serial")
public class PageNotFoundPage extends BaseErrorPage {

	@Override
	protected String getPageTitle() {
		return "404 - Page not found";
	}

	@Override
	protected int getErrorCode() {
		return HttpStatus.SC_NOT_FOUND;
	}

}
