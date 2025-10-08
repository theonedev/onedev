package io.onedev.server.service;

import io.onedev.server.model.AccessToken;
import io.onedev.server.model.User;

import org.jspecify.annotations.Nullable;

public interface AccessTokenService extends EntityService<AccessToken> {

	@Nullable
	AccessToken findByOwnerAndName(User owner, String name);
	
	@Nullable
    AccessToken findByValue(String value);

	void createOrUpdate(AccessToken projectToken);

	String createTemporal(Long userId, long secondsToExpire);
	
}
