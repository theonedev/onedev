package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface ConfigurationManager extends EntityManager<Configuration> {

	@Nullable
	Configuration find(Project project, String name);
	
	void save(Configuration configuration, @Nullable String oldName);

}
