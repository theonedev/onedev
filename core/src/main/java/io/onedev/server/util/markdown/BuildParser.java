package io.onedev.server.util.markdown;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

public class BuildParser extends ReferenceParser<Build> {

	@Override
	protected Build findReferenceable(Project project, long number) {
		return OneDev.getInstance(BuildManager.class).find(project, number);
	}
	
}
