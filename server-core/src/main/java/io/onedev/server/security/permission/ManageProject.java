package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageProject implements WritePermission {

	@Override
	public boolean implies(Permission p) {
		return true;
	}
}
