package io.onedev.server.manager;

import java.util.Collection;

import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.persistence.dao.EntityManager;

public interface EntityLabelManager<T extends EntityLabel> extends EntityManager<T> {
	
	void sync(LabelSupport<T> entity, Collection<String> labelNames);
	
}
