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
		
		add(new ExternalLink("zip", "/downloads/agent.zip"));
		add(new ExternalLink("targz", "/downloads/agent.tar.gz"));
		add(new BookmarkablePageLink<Void>("systemSetting", SystemSettingPage.class));
		add(new ExternalLink("installationGuide", OneDev.getInstance().getDocRoot() + "/agent-installation-guide.md"));
	}
	
}
