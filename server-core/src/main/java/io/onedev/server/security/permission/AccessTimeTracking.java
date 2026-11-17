package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class AccessTimeTracking implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof AccessTimeTracking || new AccessProject().implies(p);
	}
}
