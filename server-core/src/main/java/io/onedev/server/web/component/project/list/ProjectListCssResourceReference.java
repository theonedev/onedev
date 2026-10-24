package io.onedev.server.web.component.project.list;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class ProjectListCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectListCssResourceReference() {
		super(ProjectListCssResourceReference.class, "project-list.css");
	}

}
