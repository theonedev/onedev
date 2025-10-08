package io.onedev.server.entityreference;

import io.onedev.server.model.Project;

import org.jspecify.annotations.Nullable;

public class IssueReference extends EntityReference {

	private static final long serialVersionUID = 1L;
	
	public static final String TYPE = "issue";
	
	public IssueReference(Project project, Long number) {
		super(project, number);
	}

	public IssueReference(Long projectId, Long number) {
		super(projectId, number);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public static IssueReference of(String referenceString, @Nullable Project currentProject) {
		return (IssueReference) of(TYPE, referenceString, currentProject);
	}
	
}
