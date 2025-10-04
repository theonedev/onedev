package io.onedev.server.plugin.pack.container;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class ContainerHelpPanel extends Panel {
	
	private final String projectPath;
	
	public ContainerHelpPanel(String componentId, String projectPath) {
		super(componentId);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		add(new CodeSnippetPanel("loginCommand", Model.of("$ docker login " + server)));
		add(new CodeSnippetPanel("pushCommand", Model.of("$ docker push " + server + "/" + projectPath + "/<repository>:<tag>")));
		add(new InsecureRegistryNotePanel("insecureRegistryNote"));
	}
}
