package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

/** Allows otherwise authorized changes while a project is in maintenance mode. */
public class MaintainProject implements BasePermission {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof MaintainProject;
	}
}
