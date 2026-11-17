package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class WritePack implements WritePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof WritePack || new ReadPack().implies(p);
	}
}
