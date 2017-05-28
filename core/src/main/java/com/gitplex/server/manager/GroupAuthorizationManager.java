package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.persistence.dao.EntityManager;

public interface GroupAuthorizationManager extends EntityManager<GroupAuthorization> {
	
	void delete(Collection<GroupAuthorization> authorizations);
	
}
