package com.pmease.gitop.core.model.permission.system;

import org.apache.shiro.authz.Permission;

@SuppressWarnings("serial")
public class CreateTeam implements SystemPermission {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof CreateTeam;
	}

}
