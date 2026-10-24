package io.onedev.server.web.page.project;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ProjectResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectResourceReference() {
		super(ProjectResourceReference.class, "project.js");
	}

}
