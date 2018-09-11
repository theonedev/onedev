package io.onedev.server.manager;

import java.util.Set;

import io.onedev.server.model.Project;

public interface IssueInfoManager {

	public Set<String> getFixedInBuildUUIDs(Project project, String issueUUID);
	
}
