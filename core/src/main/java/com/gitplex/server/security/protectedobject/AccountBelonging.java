package com.gitplex.server.security.protectedobject;

import com.gitplex.server.model.Account;

public interface AccountBelonging extends ProtectedObject {
	Account getAccount();
}
