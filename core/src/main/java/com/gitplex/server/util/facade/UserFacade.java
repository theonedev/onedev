package com.gitplex.server.util.facade;

import javax.annotation.Nullable;

import com.gitplex.server.model.User;
import com.gitplex.server.util.StringUtils;

public class UserFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;
	
	private final String email;
	
	private final String uuid;
	
	public UserFacade(User user) {
		super(user.getId());

		name = user.getName();
		fullName = user.getFullName();
		email = user.getEmail();
		uuid = user.getUUID();
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}
	
	public String getUUID() {
		return uuid;
	}

	public boolean isRoot() {
		return getId().equals(User.ROOT_ID);
	}

	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}
	
	public boolean matchesQuery(@Nullable String queryTerm) {
		return StringUtils.matchesQuery(name, queryTerm) || StringUtils.matchesQuery(fullName, queryTerm); 
	}
	
}
