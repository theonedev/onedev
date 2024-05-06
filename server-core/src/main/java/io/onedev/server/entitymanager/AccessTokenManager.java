package io.onedev.server.entitymanager;

import io.onedev.server.model.AccessToken;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface AccessTokenManager extends EntityManager<AccessToken> {

	@Nullable
	AccessToken findByOwnerAndName(User owner, String name);
	
	@Nullable
    AccessToken findByValue(String value);

	void createOrUpdate(AccessToken projectToken);

	String createTemporal(Long userId, long secondsToExpire);
	
}
