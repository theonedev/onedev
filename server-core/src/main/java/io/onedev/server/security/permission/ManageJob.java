package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageJob implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageJob || new RunJob().implies(p);
	}

}
