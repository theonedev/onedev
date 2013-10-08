package com.pmease.gitop.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.gitop.web.page.BasePage;

@SuppressWarnings("serial")
public class TestPage2 extends BasePage {

	public TestPage2() {
		if (TestPage.ready)
			redirectToOriginal();
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				
			}
			
		});
		add(new Link<Void>("complete") {

			@Override
			public void onClick() {
				TestPage.ready = true;
				setResponsePage(TestPage2.class);
			}
			
		});
	}
	
	@Override
	protected String getPageTitle() {
		return "Test page used by Robin";
	}

}
