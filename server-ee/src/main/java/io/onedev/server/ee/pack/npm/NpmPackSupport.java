package io.onedev.server.ee.pack.npm;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.LoadableDetachableModel;

public class NpmPackSupport implements PackSupport {
	
	public static final String TYPE = "NPM";

	@Override
	public int getOrder() {
		return 150;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "npm";
	}

	@Override
	public String getProjectSeparator() {
		return ":";
	}

	@Override
	public String getReference(Pack pack) {
		return pack.getName() + "@" + pack.getVersion();
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var packId = pack.getId();
		return new NpmPackPanel(componentId, new LoadableDetachableModel<Pack>() {
			@Override
			protected Pack load() {
				return OneDev.getInstance(PackManager.class).load(packId);
			}
			
		});
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new NpmHelpPanel(componentId, project.getPath());
	}
	
}
