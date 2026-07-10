package io.onedev.server.plugin.pack.cargo;

import io.onedev.server.OneDev;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.service.PackService;
import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

public class CargoPackSupport implements PackSupport {

	public static final String TYPE = "Cargo";

	@Override
	public int getOrder() {
		return 450;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "package";
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
		return new CargoPackPanel(componentId, new LoadableDetachableModel<>() {
			@Override
			protected Pack load() {
				return OneDev.getInstance(PackService.class).load(packId);
			}
		});
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new CargoHelpPanel(componentId, project.getPath());
	}
}
