package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.BaseAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;

public interface BaseAuthorizationService extends EntityService<BaseAuthorization> {

	void syncRoles(Project project, Collection<Role> roles);
			
	void create(BaseAuthorization authorization);
	
}
