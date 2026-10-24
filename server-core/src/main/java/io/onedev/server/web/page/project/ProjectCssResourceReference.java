package io.onedev.server.web.page.project;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class ProjectCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectCssResourceReference() {
		super(ProjectCssResourceReference.class, "project.css");
	}

}
