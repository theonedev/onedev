package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
//		System.out.println(RequestCycle.get().getUrlRenderer().renderRelativeUrl(Url.parse("upload")));
	}		

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
}
