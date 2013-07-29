package com.pmease.gitop.core.model.permission.system;

import org.apache.shiro.authz.Permission;

@SuppressWarnings("serial")
public class SystemAdministration implements SystemPermission {

	@Override
	public boolean implies(Permission p) {
		return true;
	}

}
