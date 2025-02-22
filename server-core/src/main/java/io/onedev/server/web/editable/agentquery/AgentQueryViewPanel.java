package io.onedev.server.web.editable.agentquery;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.page.admin.buildsetting.agent.AgentListPage;

public class AgentQueryViewPanel extends Panel {

	private final String queryString;
	
	public AgentQueryViewPanel(String id, String queryString) {
		super(id);
		this.queryString = queryString;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (queryString != null) 
			add(new Label("queryString", queryString));
		else
			add(new Label("queryString", "<i>Any agent</i>").setEscapeModelStrings(false));
		
		add(new BookmarkablePageLink<Void>("showSelectedAgents", 
				AgentListPage.class, AgentListPage.paramsOf(queryString, 0)));
	}

}
