package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageWorkspaces implements WritePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageWorkspaces || new CreateWorkspaces().implies(p) || new WriteCode().implies(p);
	}
}
