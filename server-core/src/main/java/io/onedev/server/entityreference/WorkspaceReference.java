package io.onedev.server.entityreference;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;

public class WorkspaceReference extends EntityReference {

	private static final long serialVersionUID = 1L;

	public static final String TYPE = "workspace";

	public WorkspaceReference(Project project, Long number) {
		super(project, number);
	}

	public WorkspaceReference(Long projectId, Long number) {
		super(projectId, number);
	}

	public static WorkspaceReference of(String referenceString, @Nullable Project currentProject) {
		return (WorkspaceReference) of(TYPE, referenceString, currentProject);
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
