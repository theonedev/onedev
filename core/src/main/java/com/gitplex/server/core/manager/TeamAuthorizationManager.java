package com.gitplex.server.core.manager;

import java.util.Collection;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.TeamAuthorization;

public interface TeamAuthorizationManager extends EntityManager<TeamAuthorization> {
	
	void delete(Collection<TeamAuthorization> authorizations);

	Collection<TeamAuthorization> findAll(Account organization);
}
