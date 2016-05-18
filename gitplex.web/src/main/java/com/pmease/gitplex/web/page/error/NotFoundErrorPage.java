package com.pmease.gitplex.web.page.error;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.basic.Label;

@SuppressWarnings("serial")
public class NotFoundErrorPage extends BaseErrorPage {

	private final String errorMessage;
	
	public NotFoundErrorPage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("errorMessage", errorMessage));
	}

	@Override
	protected int getErrorCode() {
		return HttpServletResponse.SC_NOT_FOUND;
	}

}
