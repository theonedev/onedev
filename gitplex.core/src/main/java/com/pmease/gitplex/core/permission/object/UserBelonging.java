package com.pmease.gitplex.core.permission.object;

import com.pmease.gitplex.core.entity.User;

public interface UserBelonging extends ProtectedObject {
	User getUser();
}
