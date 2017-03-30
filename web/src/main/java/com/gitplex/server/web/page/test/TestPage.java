package com.gitplex.server.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;

import com.gitplex.server.web.component.markdown.MarkdownEditor;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new MarkdownEditor("markdownEditor", Model.of(""), false, true) {

			@Override
			protected String getAutosaveKey() {
				return "test";
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
