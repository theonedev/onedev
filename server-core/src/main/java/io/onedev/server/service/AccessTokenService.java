package io.onedev.server.service;

import io.onedev.server.model.AccessToken;

import org.jspecify.annotations.Nullable;

public interface AccessTokenService extends EntityService<AccessToken> {

	@Nullable
    AccessToken findByValue(String value);

	void createOrUpdate(AccessToken token);

	String createTemporal(Long userId, long secondsToExpire);
	
}
