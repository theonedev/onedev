package io.onedev.server.util.facade;

import javax.annotation.Nullable;

public class UserFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;
	
	private final String accessToken;
	
	public UserFacade(Long id, String name, @Nullable String fullName, String accessToken) {
		super(id);
		this.name = name;
		this.fullName = fullName;
		this.accessToken = accessToken;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}
	
}
