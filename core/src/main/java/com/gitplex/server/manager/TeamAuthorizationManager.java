package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.TeamAuthorization;
import com.gitplex.server.persistence.dao.EntityManager;

public interface TeamAuthorizationManager extends EntityManager<TeamAuthorization> {
	
	void delete(Collection<TeamAuthorization> authorizations);

	Collection<TeamAuthorization> findAll(Account organization);
}
