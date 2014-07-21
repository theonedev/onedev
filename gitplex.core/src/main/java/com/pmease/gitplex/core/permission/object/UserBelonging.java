package com.pmease.gitplex.core.permission.object;

import com.pmease.gitplex.core.model.User;

public interface UserBelonging extends ProtectedObject {
	User getUser();
}
