package com.gitplex.server.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.UserAuthorization;
import com.gitplex.server.persistence.dao.EntityManager;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {
	
	@Nullable
	UserAuthorization find(Account user, Depot depot);
	
	void delete(Collection<UserAuthorization> authorizations);

	Collection<UserAuthorization> findAll(Account account);
	
}