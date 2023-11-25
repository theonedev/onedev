package io.onedev.server.ee.pack.container;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.util.UrlUtils;
import org.apache.wicket.Component;

import javax.inject.Singleton;

@Singleton
public class ContainerPackSupport implements PackSupport {

	public static final String TYPE = "Container Image";
	
	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "docker";
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		return new ContainerPackPanel(componentId, server + "/" + pack.getProject().getPath(), 
				pack.getVersion(), pack.getBlobHash());
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new ContainerHelpPanel(componentId, project.getPath());
	}

}
