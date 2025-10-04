package io.onedev.server.plugin.pack.helm;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.PackService;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;

public class HelmPackSupport implements PackSupport {

	public static final String TYPE = "Helm";
	
	@Override
	public int getOrder() {
		return 500;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "helm";
	}

	@Override
	public String getReference(Pack pack, boolean withProject) {
		var reference = pack.getName() + "-" + pack.getVersion();
		if (withProject)
			reference = pack.getProject().getPath() + ":" + reference;
		return reference;
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var packId = pack.getId();
		return new HelmPackPanel(componentId, new LoadableDetachableModel<>() {
			@Override
			protected Pack load() {
				return OneDev.getInstance(PackService.class).load(packId);
			}
		});
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new HelmHelpPanel(componentId, project.getPath());
	}
}
