package io.onedev.server.security.permission;

import io.onedev.server.model.User;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class ManageIssues implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageIssues 
				|| new AccessConfidentialIssues().implies(p)
				|| new EditIssueField(null).implies(p) 
				|| new EditIssueLink(null).implies(p) 
				|| new ScheduleIssues().implies(p);
	}

	@Override
	public boolean isApplicable(@Nullable User user) {
		return user != null && !user.isEffectiveGuest();
	}
}
