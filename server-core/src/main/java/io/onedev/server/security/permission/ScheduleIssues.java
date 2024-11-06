package io.onedev.server.security.permission;

import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class ScheduleIssues implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ScheduleIssues || new AccessProject().implies(p);
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
