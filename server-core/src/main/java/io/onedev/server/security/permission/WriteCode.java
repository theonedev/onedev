package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class WriteCode implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof WriteCode || new ReadCode().implies(p);
	}

}
