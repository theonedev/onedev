package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;
import org.jspecify.annotations.Nullable;

import io.onedev.server.util.facade.UserFacade;

/** Allows otherwise authorized changes while a project is in maintenance mode. */
public class MaintainProject implements BasePermission {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof MaintainProject;
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
