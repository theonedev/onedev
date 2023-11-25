package io.onedev.server.ee.pack.container;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.util.UrlUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class InsecureRegistryNotePanel extends Panel {
	
	public InsecureRegistryNotePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("server", UrlUtils.getServer(getServerUrl())));
	}

	private String getServerUrl() {
		return OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getServerUrl().startsWith("http://"));
	}
	
}
