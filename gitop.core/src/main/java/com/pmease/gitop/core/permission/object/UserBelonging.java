package com.pmease.gitop.core.permission.object;

import com.pmease.gitop.core.model.User;

public interface UserBelonging extends ProtectedObject {
	User getUser();
}
