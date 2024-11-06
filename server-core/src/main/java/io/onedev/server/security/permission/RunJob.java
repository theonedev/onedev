package io.onedev.server.security.permission;

import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class RunJob implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof RunJob 
				|| new AccessBuildLog().implies(p) 
				|| new AccessBuildPipeline().implies(p)
				|| new AccessBuildReports("*").implies(p);
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
