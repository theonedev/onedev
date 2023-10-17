package io.onedev.server.entitymanager;

import io.onedev.server.model.ProjectLabel;

public interface ProjectLabelManager extends EntityLabelManager<ProjectLabel> {
	
	void create(ProjectLabel projectLabel);
	
}
