package com.pmease.gitplex.core.permission.object;

import com.pmease.gitplex.core.entity.Account;

public interface AccountBelonging extends ProtectedObject {
	Account getOwner();
}
