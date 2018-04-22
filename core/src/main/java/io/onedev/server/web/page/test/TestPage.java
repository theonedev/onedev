package io.onedev.server.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				OneDev.getInstance(IssueManager.class).test();
			}
			
		});
	}

}
