package io.onedev.server.web.page.test;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.base.BasePage;

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
				OneDev.getInstance(ProjectManager.class).hasChildren(9L);
			}

		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}

}
