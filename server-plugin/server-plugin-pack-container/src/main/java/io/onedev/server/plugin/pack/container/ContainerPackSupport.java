package io.onedev.server.plugin.pack.container;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.util.UrlUtils;
import org.apache.wicket.Component;

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
	public String getReference(Pack pack, boolean withProject) {
		var reference = pack.getName() + ":" + pack.getVersion();
		if (withProject)
			reference = pack.getProject().getPath() + "/" + reference;
		return reference;
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		return new ContainerPackPanel(componentId, pack.getProject().getId(), server + "/" + pack.getProject().getPath().toLowerCase() + "/" + pack.getName(), 
				pack.getVersion(), (String) pack.getData());
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new ContainerHelpPanel(componentId, project.getPath());
	}

}
