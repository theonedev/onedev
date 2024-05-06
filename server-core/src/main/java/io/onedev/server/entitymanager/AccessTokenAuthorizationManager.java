package io.onedev.server.entitymanager;

import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface AccessTokenAuthorizationManager extends EntityManager<AccessTokenAuthorization> {

	void syncAuthorizations(AccessToken token, Collection<AccessTokenAuthorization> authorizations);
	
    void createOrUpdate(AccessTokenAuthorization authorization);
	
}