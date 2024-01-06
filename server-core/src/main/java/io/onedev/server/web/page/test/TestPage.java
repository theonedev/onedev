package io.onedev.server.web.page.test;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
				var project = OneDev.getInstance(ProjectManager.class).load(1L);
				System.out.println("************************");
				for (var pack: OneDev.getInstance(PackManager.class).queryLatests(project, "npm", "mylib", 0, 100)) {
					System.out.println(pack.getName() + "@" + pack.getVersion());
				}
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
