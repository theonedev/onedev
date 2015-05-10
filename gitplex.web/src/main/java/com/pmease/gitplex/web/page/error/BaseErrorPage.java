package com.pmease.gitplex.web.page.error;

import org.apache.wicket.request.http.WebResponse;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class BaseErrorPage extends BasePage {
	protected BaseErrorPage() {
	}
	
	protected abstract int getErrorCode();
	
	@Override
	protected void configureResponse(final WebResponse response) {
		super.configureResponse(response);
		response.setStatus(getErrorCode());
	}
	
	@Override
	public boolean isErrorPage() {
		return true;
	}
}
