package com.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.UserAuthorization;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {
	
	@Nullable
	UserAuthorization find(Account user, Depot depot);
	
	void delete(Collection<UserAuthorization> authorizations);

	Collection<UserAuthorization> findAll(Account account);
	
}