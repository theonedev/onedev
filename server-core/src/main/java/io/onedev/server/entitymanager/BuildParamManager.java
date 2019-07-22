package io.onedev.server.entitymanager;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildParamManager extends EntityManager<BuildParam> {
	
	void deleteParams(Build build);
	
}
