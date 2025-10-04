package io.onedev.server.service;

import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;

import java.util.Collection;

public interface AccessTokenAuthorizationService extends EntityService<AccessTokenAuthorization> {

	void syncAuthorizations(AccessToken token, Collection<AccessTokenAuthorization> authorizations);
	
    void createOrUpdate(AccessTokenAuthorization authorization);
	
}