package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.dao.EntityManager;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {

	void syncAuthorizations(User user, Collection<UserAuthorization> authorizations);
	
	void syncAuthorizations(Project project, Collection<UserAuthorization> authorizations);
	
}