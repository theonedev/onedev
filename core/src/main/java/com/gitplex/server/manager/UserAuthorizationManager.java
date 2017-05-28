package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.persistence.dao.EntityManager;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {
	
	void delete(Collection<UserAuthorization> authorizations);

}