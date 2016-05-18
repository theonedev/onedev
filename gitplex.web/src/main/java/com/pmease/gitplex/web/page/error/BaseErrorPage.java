package com.pmease.gitplex.web.page.error;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.http.WebResponse;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class BaseErrorPage extends BasePage {
	
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

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		target.appendJavaScript("location.reload();");
	}
	
}
