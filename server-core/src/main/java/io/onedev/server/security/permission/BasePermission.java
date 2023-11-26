package io.onedev.server.security.permission;

import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;

import javax.annotation.Nullable;

public interface BasePermission extends Permission {
	
	boolean isApplicable(@Nullable UserFacade user);
	
}
