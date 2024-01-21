package io.onedev.server.entitymanager;

import io.onedev.server.model.BuildLabel;

public interface BuildLabelManager extends EntityLabelManager<BuildLabel> {

	void create(BuildLabel buildLabel);
	
}
