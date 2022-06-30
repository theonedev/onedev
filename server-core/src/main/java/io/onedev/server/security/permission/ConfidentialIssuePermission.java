package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

import io.onedev.server.model.Issue;

public class ConfidentialIssuePermission implements Permission {

	private final Issue issue;
	
	public ConfidentialIssuePermission(Issue issue) {
		this.issue = issue;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof ConfidentialIssuePermission) {
			ConfidentialIssuePermission issuePermission = (ConfidentialIssuePermission) p;
			return issue.equals(issuePermission.issue);
		} else {
			return false;
		}
	}

}
