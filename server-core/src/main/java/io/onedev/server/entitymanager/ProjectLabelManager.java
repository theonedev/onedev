package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;

import java.util.Collection;

public interface ProjectLabelManager extends EntityLabelManager<ProjectLabel> {
	
	void create(ProjectLabel projectLabel);

	void populateLabels(Collection<Project> projects);
	
}
