package com.pmease.gitop.core.model.permission.system;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.permission.account.AccountPermission;
import com.pmease.gitop.core.model.permission.account.WriteToAccount;

@SuppressWarnings("serial")
public class WriteToAllAccounts implements SystemPermission {

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof WriteToAllAccounts || new ReadFromAllAccounts().implies(permission)) {
			return true;
		} else if (permission instanceof AccountPermission) {
			AccountPermission accountPermission = (AccountPermission) permission;
			return new WriteToAccount().can(accountPermission.getOperation());
		} else {
			return false;
		}
	}

}
