package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class AccessBuildLog implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof AccessBuildLog || new AccessBuildReports("*").implies(p);
	}

}
