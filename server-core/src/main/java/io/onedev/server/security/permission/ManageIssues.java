package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageIssues implements WritePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageIssues 
				|| new AccessTimeTracking().implies(p)
				|| new AccessConfidentialIssues().implies(p)
				|| new EditIssueField(null).implies(p) 
				|| new EditIssueLink(null).implies(p) 
				|| new EditFieldsOfOtherIssues().implies(p)
				|| new ScheduleIssues().implies(p);
	}
}
