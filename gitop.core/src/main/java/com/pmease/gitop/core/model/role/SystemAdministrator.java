package com.pmease.gitop.core.model.role;

import org.apache.shiro.authz.Permission;

@SuppressWarnings("serial")
public class SystemAdministrator implements Role {

	@Override
	public boolean implies(Permission p) {
		return true;
	}

}
