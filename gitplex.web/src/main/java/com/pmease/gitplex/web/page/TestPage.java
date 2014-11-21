package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.manager.VerificationManager;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				System.out.println(AppLoader.injector.getInstance(VerificationManager.class));
			}
			
		});
	}

}
