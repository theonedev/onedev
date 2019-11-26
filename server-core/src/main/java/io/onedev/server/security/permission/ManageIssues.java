package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class ManageIssues implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof ManageIssues || new EditIssueField(null).implies(p) || new ScheduleIssues().implies(p);
	}

}
