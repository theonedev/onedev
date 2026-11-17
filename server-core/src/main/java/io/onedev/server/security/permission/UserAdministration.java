package io.onedev.server.security.permission;

import io.onedev.server.model.User;
import org.apache.shiro.authz.Permission;

public class UserAdministration implements BasePermission {
	
	private final User user;

	public UserAdministration(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public boolean implies(Permission p) {
		if (p instanceof UserAdministration) {
			UserAdministration userAdmin = (UserAdministration) p;
			return user.equals(userAdmin.getUser());
		} else {
			return false;
		}
	}
}
