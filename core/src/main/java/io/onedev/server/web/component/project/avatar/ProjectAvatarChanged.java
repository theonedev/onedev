package io.onedev.server.web.component.project.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.Project;
import io.onedev.server.web.util.AjaxPayload;

public class ProjectAvatarChanged extends AjaxPayload {

	private final Project project;
	
	public ProjectAvatarChanged(AjaxRequestTarget target, Project project) {
		super(target);
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

}