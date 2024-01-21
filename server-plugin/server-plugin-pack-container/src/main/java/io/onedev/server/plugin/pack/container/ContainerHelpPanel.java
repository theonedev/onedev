package io.onedev.server.plugin.pack.container;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.util.UrlUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class ContainerHelpPanel extends Panel {
	
	private final String projectPath;
	
	public ContainerHelpPanel(String componentId, String projectPath) {
		super(componentId);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		add(new Label("loginCommand", "docker login " + server));
		add(new Label("pushCommand", "docker push " + server + "/" + projectPath + "/<repository>:<tag>"));
		add(new InsecureRegistryNotePanel("insecureRegistryNote"));
	}
}
