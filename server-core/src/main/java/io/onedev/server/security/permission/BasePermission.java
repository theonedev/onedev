package io.onedev.server.security.permission;

import io.onedev.server.model.User;
import org.apache.shiro.authz.Permission;

import javax.annotation.Nullable;

public interface BasePermission extends Permission {
	
	boolean isApplicable(@Nullable User user);
	
}
