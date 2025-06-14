package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.BaseAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityManager;

public interface BaseAuthorizationManager extends EntityManager<BaseAuthorization> {

	void syncRoles(Project project, Collection<Role> roles);
			
	void create(BaseAuthorization authorization);
	
}
