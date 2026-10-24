package io.onedev.server.web.component.project.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ProjectChoiceResourceReference extends BaseDependentResourceReference {
	
	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectChoiceResourceReference() {
		super(ProjectChoiceResourceReference.class, "project-choice.js");
	}

}
