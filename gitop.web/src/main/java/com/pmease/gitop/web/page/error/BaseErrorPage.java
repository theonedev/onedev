package com.pmease.gitop.web.page.error;

import org.apache.wicket.request.http.WebResponse;

import com.pmease.gitop.web.page.EmptyPage;

@SuppressWarnings("serial")
public abstract class BaseErrorPage extends EmptyPage {
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
