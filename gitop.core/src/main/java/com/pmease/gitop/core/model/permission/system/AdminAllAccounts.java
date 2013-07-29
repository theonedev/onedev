package com.pmease.gitop.core.model.permission.system;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.permission.account.AccountPermission;

@SuppressWarnings("serial")
public class AdminAllAccounts implements SystemPermission {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof AdminAllAccounts 
				|| permission instanceof AccountPermission
				|| new WriteToAllAccounts().implies(permission); 
	}

}
