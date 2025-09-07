package io.onedev.server.entitymanager;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildParamManager extends EntityManager<BuildParam> {
	
	void create(BuildParam param);
	
	Collection<String> getParamNames(@Nullable Project project);

}
