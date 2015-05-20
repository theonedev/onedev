package com.pmease.gitplex.web.page.home.dashboard;

import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.gitplex.web.page.layout.LayoutPage;

public class DashboardPage extends LayoutPage {

	private static final long serialVersionUID = 1L;
	
	public DashboardPage() {
		this.setStatelessHint(true);
	}

	@Override
	protected String getPageTitle() {
		return "GitPlex - Dashboard";
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
