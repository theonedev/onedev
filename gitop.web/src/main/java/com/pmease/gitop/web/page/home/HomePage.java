package com.pmease.gitop.web.page.home;

import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.gitop.web.page.layout.LayoutPage;

public class HomePage extends LayoutPage {

	private static final long serialVersionUID = 1L;
	
	public HomePage() {
		this.setStatelessHint(true);
	}

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
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
