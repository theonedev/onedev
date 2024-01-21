package io.onedev.server.plugin.pack.maven;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackSupport;
import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import static io.onedev.server.plugin.pack.maven.MavenPackService.NONE;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class MavenPackSupport implements PackSupport {

	public static final String TYPE = "Maven";
	
	@Override
	public int getOrder() {
		return 200;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "maven";
	}

	@Override
	public String getReference(Pack pack, boolean withProject) {
		String reference;
		if (!pack.getName().endsWith(NONE) && !pack.getVersion().equals(NONE))
			reference = pack.getName() + ":" + pack.getVersion();
		else 
			reference = substringBeforeLast(pack.getName(), ":") + ":<Plugins Metadata>";
		
		if (withProject)
			reference = pack.getProject().getPath() + ">" + reference;
		return reference;
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var packId = pack.getId();
		return new MavenPackPanel(componentId, new LoadableDetachableModel<>() {
			@Override
			protected Pack load() {
				return OneDev.getInstance(PackManager.class).load(packId);
			}
			
		});
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new MavenHelpPanel(componentId, project.getPath());
	}

}
