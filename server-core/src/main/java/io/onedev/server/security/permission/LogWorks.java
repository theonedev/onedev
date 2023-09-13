package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class LogWorks implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof LogWorks || new AccessProject().implies(p);
	}

}
