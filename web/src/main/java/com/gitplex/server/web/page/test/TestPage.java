package com.gitplex.server.web.page.test;

import org.apache.wicket.model.Model;

import com.gitplex.server.web.component.markdown.MarkdownViewer;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new MarkdownViewer("markdown", Model.of("abc [child](img/child.png)"), null) {

			@Override
			protected String getBaseUrl() {
				return "/hello";
			}
			
		});
	}

}
