package io.onedev.server.entityreference;

import io.onedev.server.model.Project;

import org.jspecify.annotations.Nullable;

public class PullRequestReference extends EntityReference {

	private static final long serialVersionUID = 1L;
	
	public static final String TYPE = "pull request";
	
	public PullRequestReference(Project project, Long number) {
		super(project, number);
	}

	public PullRequestReference(Long projectId, Long number) {
		super(projectId, number);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public static PullRequestReference of(String referenceString, @Nullable Project currentProject) {
		return (PullRequestReference) of(TYPE, referenceString, currentProject);
	}
	
}
