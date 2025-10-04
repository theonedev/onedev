package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.model.support.LabelSupport;

public interface EntityLabelService<T extends EntityLabel> extends EntityService<T> {
	
	void sync(LabelSupport<T> entity, Collection<String> labelNames);
	
}
