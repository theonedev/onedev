package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class CreateChildren implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof CreateChildren || new AccessProject().implies(p);
	}

}
