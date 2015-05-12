package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.gitplex.web.page.layout.MaintabPage;

public class HomePage extends MaintabPage {

	private static final long serialVersionUID = 1L;
	
	public HomePage() {
		this.setStatelessHint(true);
	}

	@Override
	protected String getPageTitle() {
		return "GitPlex - Home";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
