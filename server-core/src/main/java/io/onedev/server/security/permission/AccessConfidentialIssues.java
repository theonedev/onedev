package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

public class AccessConfidentialIssues implements Permission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof AccessConfidentialIssues 
				|| p instanceof ConfidentialIssuePermission 
				|| new AccessProject().implies(p);
	}

}
