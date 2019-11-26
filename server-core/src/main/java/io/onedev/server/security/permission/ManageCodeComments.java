package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageCodeComments implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageCodeComments || new WriteCode().implies(p);
	}

}
