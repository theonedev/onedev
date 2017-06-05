package com.gitplex.server.security.permission;

import org.apache.shiro.authz.Permission;

import com.gitplex.server.util.facade.UserFacade;

public class UserAdministration implements Permission {
	
	private final UserFacade user;

	public UserAdministration(UserFacade user) {
		this.user = user;
	}

	public UserFacade getUser() {
		return user;
	}

	@Override
	public boolean implies(Permission p) {
		if (p instanceof UserAdministration) {
			UserAdministration userAdmin = (UserAdministration) p;
			return user.equals(userAdmin.getUser());
		} else {
			return p instanceof PublicPermission;
		}
	}
	
}
