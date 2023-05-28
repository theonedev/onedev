package io.onedev.server.entitymanager;

import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.ProjectLabel;

public interface BuildLabelManager extends EntityLabelManager<BuildLabel> {

	void create(BuildLabel buildLabel);
	
}
