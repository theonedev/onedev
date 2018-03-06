package io.onedev.server.manager;

import java.util.Collection;

import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.dao.EntityManager;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {
	
	void delete(Collection<UserAuthorization> authorizations);

}