package io.onedev.server.manager;

import java.util.Collection;

import io.onedev.server.model.Project;

public interface BuildInfoManager {

	Collection<Long> getFixBuildIds(Project project, Long issueNumber);
	
}
