package io.onedev.server.security.permission;

import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

import io.onedev.server.util.facade.UserFacade;

public class ConfidentialIssuesPermission implements BasePermission {

	private final Set<Long> issueIds;
	
	public ConfidentialIssuesPermission(Set<Long> issueIds) {
		this.issueIds = issueIds;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof ConfidentialIssuePermission) {
			ConfidentialIssuePermission issuePermission = (ConfidentialIssuePermission) p;
			return issueIds.contains(issuePermission.getIssue().getId());
		} else if (p instanceof ConfidentialIssuesPermission) {
			ConfidentialIssuesPermission issuesPermission = (ConfidentialIssuesPermission) p;
			return issueIds.containsAll(issuesPermission.getIssueIds());
		} else {
			return false;
		}
	}

	public Set<Long> getIssueIds() {
		return issueIds;
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
	
}
