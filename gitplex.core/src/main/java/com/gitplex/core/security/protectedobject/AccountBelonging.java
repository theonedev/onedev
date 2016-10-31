package com.gitplex.core.security.protectedobject;

import com.gitplex.core.entity.Account;

public interface AccountBelonging extends ProtectedObject {
	Account getAccount();
}
