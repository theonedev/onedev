package io.onedev.server.service;

import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;

import java.util.Collection;

public interface ProjectLabelService extends EntityLabelService<ProjectLabel> {
	
	void create(ProjectLabel projectLabel);

	void populateLabels(Collection<Project> projects);
	
}
