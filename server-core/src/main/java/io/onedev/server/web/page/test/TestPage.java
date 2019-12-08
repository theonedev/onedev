package io.onedev.server.web.page.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.util.Pair;
import io.onedev.server.web.page.base.BasePage;

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
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}
	
	public static class Bean {
		public Map<String, VersionedDocument> doms = new HashMap<>();
		
		public List<String> names = new ArrayList<>();
		
		public List<WebHook.EventType> events = new ArrayList<>();
		
		public VersionedDocument dom;
		
		public WebHook.EventType event;
		
		public Pair<String, String> pair = new Pair<>("fi\0rst", "second");
	}
}
