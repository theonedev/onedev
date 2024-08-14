package io.onedev.server.event.project;

import java.util.Date;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Translation;

public class ProjectCreated extends ProjectEvent {
	
	public ProjectCreated(Project project) {
		super(SecurityUtils.getUser(), new Date(), project);
	}

	@Override
	public String getActivity() {
		return Translation.get("Created");
	}

}
