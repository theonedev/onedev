package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
