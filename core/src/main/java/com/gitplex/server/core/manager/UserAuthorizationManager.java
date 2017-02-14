package com.gitplex.server.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.UserAuthorization;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {
	
	@Nullable
	UserAuthorization find(Account user, Depot depot);
	
	void delete(Collection<UserAuthorization> authorizations);

	Collection<UserAuthorization> findAll(Account account);
	
}