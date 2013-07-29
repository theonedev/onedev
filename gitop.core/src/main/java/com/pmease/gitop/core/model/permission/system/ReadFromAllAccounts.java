package com.pmease.gitop.core.model.permission.system;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.permission.account.AccountPermission;
import com.pmease.gitop.core.model.permission.account.ReadFromAccount;

@SuppressWarnings("serial")
public class ReadFromAllAccounts implements SystemPermission {

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ReadFromAllAccounts 
				|| new CreateRepository().implies(permission) 
				|| new CreateTeam().implies(permission)) {
			return true;
		} else if (permission instanceof AccountPermission) {
			AccountPermission accountPermission = (AccountPermission) permission;
			return new ReadFromAccount().can(accountPermission.getOperation());
		} else {
			return false;
		}
	}

}
