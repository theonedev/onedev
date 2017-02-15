package com.gitplex.server.security.protectedobject;

import com.gitplex.server.entity.Account;

public interface AccountBelonging extends ProtectedObject {
	Account getAccount();
}
