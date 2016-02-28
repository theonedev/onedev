package com.pmease.gitplex.core.permission.object;

import com.pmease.gitplex.core.entity.Account;

public interface UserBelonging extends ProtectedObject {
	Account getUser();
}
