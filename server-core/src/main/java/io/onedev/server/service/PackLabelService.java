package io.onedev.server.service;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackLabel;

import java.util.Collection;

public interface PackLabelService extends EntityLabelService<PackLabel> {

	void create(PackLabel packLabel);
	
	void populateLabels(Collection<Pack> packs);
	
}
