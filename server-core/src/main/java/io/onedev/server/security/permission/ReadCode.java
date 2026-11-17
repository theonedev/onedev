package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ReadCode implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ReadCode || new AccessProject().implies(p);
	}
}
