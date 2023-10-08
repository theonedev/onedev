package io.onedev.server.manager;

import io.onedev.server.model.BuildLabel;

public interface BuildLabelManager extends EntityLabelManager<BuildLabel> {

	void create(BuildLabel buildLabel);
	
}
