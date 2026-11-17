package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public interface BasePermission extends Permission {

	default boolean isWrite() {
		return false;
	}
	
}
