package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class CreateWorkspaces implements WritePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof CreateWorkspaces || new ReadCode().implies(p);
	}
}
