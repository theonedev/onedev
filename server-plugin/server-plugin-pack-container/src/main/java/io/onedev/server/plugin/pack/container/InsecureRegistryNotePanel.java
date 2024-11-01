package io.onedev.server.plugin.pack.container;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import org.apache.wicket.markup.html.panel.Panel;

public class InsecureRegistryNotePanel extends Panel {
	
	public InsecureRegistryNotePanel(String id) {
		super(id);
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
