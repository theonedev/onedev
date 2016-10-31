package com.gitplex.core.manager;

import java.util.Collection;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.TeamAuthorization;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface TeamAuthorizationManager extends EntityManager<TeamAuthorization> {
	
	void delete(Collection<TeamAuthorization> authorizations);

	Collection<TeamAuthorization> findAll(Account organization);
}
