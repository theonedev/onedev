package io.onedev.server.web.component.project.selector;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class ProjectSelectorCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectSelectorCssResourceReference() {
		super(ProjectSelectorCssResourceReference.class, "project-selector.css");
	}

}
