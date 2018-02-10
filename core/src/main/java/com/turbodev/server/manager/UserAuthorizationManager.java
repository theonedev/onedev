package com.turbodev.server.manager;

import java.util.Collection;

import com.turbodev.server.model.UserAuthorization;
import com.turbodev.server.persistence.dao.EntityManager;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {
	
	void delete(Collection<UserAuthorization> authorizations);

}