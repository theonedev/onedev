package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

import io.onedev.server.util.facade.UserFacade;

public class EditFieldsOfOtherIssues implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof EditFieldsOfOtherIssues;
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
