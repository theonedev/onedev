package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.TeamAuthorization;

public interface TeamAuthorizationManager extends EntityManager<TeamAuthorization> {
	
	void delete(Collection<TeamAuthorization> authorizations);

	Collection<TeamAuthorization> query(Account organization);
}
