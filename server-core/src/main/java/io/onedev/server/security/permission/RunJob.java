package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class RunJob implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof RunJob || new AccessBuildLog().implies(p);
	}

}
