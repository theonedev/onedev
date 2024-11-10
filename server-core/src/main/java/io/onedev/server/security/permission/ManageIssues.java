package io.onedev.server.security.permission;

import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class ManageIssues implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageIssues 
				|| new AccessTimeTracking().implies(p)
				|| new AccessConfidentialIssues().implies(p)
				|| new EditIssueField(null).implies(p) 
				|| new EditIssueLink(null).implies(p) 
				|| new ScheduleIssues().implies(p);
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
