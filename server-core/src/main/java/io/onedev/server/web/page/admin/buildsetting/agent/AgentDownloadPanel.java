package io.onedev.server.web.page.admin.buildsetting.agent;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;

@SuppressWarnings("serial")
class AgentDownloadPanel extends Panel {

	public AgentDownloadPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ExternalLink("agent", "/downloads/agent.zip"));
		add(new BookmarkablePageLink<Void>("systemSetting", SystemSettingPage.class));
		add(new ExternalLink("agentManagement", OneDev.getInstance().getDocRoot() + "/pages/agent-management.md"));
	}
	
}
