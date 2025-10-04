package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;

public interface UserAuthorizationService extends EntityService<UserAuthorization> {

	void syncAuthorizations(User user, Collection<UserAuthorization> authorizations);
	
	void syncAuthorizations(Project project, Collection<UserAuthorization> authorizations);
	
    void createOrUpdate(UserAuthorization authorization);
	
}