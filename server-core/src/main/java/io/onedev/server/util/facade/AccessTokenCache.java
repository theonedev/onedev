package io.onedev.server.util.facade;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.util.MapProxy;

public class AccessTokenCache extends MapProxy<Long, AccessTokenFacade> {

	private static final long serialVersionUID = 1L;
	
	public AccessTokenCache(Map<Long, AccessTokenFacade> delegate) {
		super(delegate);
	}

	@Nullable
	public AccessTokenFacade findByOwnerAndName(Long ownerId, String name) {
		for (AccessTokenFacade facade: values()) {
			if (ownerId.equals(facade.getOwnerId()) && name.equalsIgnoreCase(facade.getName()))
				return facade;
		}
		return null;
	}
	
	@Nullable
	public AccessTokenFacade findByValue(String value) {
		for (AccessTokenFacade facade: values()) {
			if (value.equals(facade.getValue()) && !facade.isExpired())
				return facade;
		}
		return null;
	}
	
}
