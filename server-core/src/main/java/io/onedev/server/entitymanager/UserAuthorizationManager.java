package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface UserAuthorizationManager extends EntityManager<UserAuthorization> {

	void syncAuthorizations(User user, Collection<UserAuthorization> authorizations);
	
	void syncAuthorizations(Project project, Collection<UserAuthorization> authorizations);

	void authorize(User user, Project project, Role role);
	
    void createOrUpdate(UserAuthorization authorization);
	
}