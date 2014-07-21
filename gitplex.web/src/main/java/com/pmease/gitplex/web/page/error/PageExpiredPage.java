package com.pmease.gitplex.web.page.error;

import org.apache.commons.httpclient.HttpStatus;

@SuppressWarnings("serial")
public class PageExpiredPage extends BaseErrorPage {

	@Override
	protected String getPageTitle() {
		return "Page Expired";
	}

	@Override
	protected int getErrorCode() {
		return HttpStatus.SC_OK; // we can recover
	}

}
