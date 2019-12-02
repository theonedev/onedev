package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageBuilds implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageBuilds || new JobPermission("*", new ManageJob()).implies(p);
	}

}
