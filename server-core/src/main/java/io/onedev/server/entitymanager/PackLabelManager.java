package io.onedev.server.entitymanager;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackLabel;

import java.util.Collection;

public interface PackLabelManager extends EntityLabelManager<PackLabel> {

	void create(PackLabel packLabel);
	
	void populateLabels(Collection<Pack> packs);
	
}
