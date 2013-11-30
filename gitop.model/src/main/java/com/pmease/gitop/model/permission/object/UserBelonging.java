package com.pmease.gitop.model.permission.object;

import com.pmease.gitop.model.User;

public interface UserBelonging extends ProtectedObject {
	User getUser();
}
