package io.onedev.server.entityreference;

import io.onedev.server.model.Project;

import org.jspecify.annotations.Nullable;

public class BuildReference extends EntityReference {

	private static final long serialVersionUID = 1L;

	public static final String TYPE = "build";

	public BuildReference(Project project, Long number) {
		super(project, number);
	}

	public BuildReference(Long projectId, Long number) {
		super(projectId, number);
	}
	
	public static BuildReference of(String referenceString, @Nullable Project currentProject) {
		return (BuildReference) of(TYPE, referenceString, currentProject);
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
