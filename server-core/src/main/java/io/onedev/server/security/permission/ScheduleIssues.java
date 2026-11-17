package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ScheduleIssues implements WritePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ScheduleIssues || new AccessProject().implies(p);
	}
}
