package com.gitplex.server.core.security.protectedobject;

import com.gitplex.server.core.entity.Account;

public interface AccountBelonging extends ProtectedObject {
	Account getAccount();
}
