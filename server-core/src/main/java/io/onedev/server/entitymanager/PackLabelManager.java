package io.onedev.server.entitymanager;

import io.onedev.server.model.PackLabel;

public interface PackLabelManager extends EntityLabelManager<PackLabel> {

	void create(PackLabel packLabel);
	
}
