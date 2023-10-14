package io.onedev.server.security.permission;

import javax.annotation.Nullable;

import io.onedev.server.model.User;
import org.apache.shiro.authz.Permission;

import io.onedev.server.model.LinkSpec;

public class EditIssueLink implements BasePermission {

	private final LinkSpec link;
	
	public EditIssueLink(@Nullable LinkSpec link) {
		this.link = link;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof EditIssueLink) {
			EditIssueLink editIssueLink = (EditIssueLink) p;
			return link == null || link.equals(editIssueLink.link);
		} else {
			return new AccessProject().implies(p);
		}
	}

	@Override
	public boolean isApplicable(@Nullable User user) {
		return user != null;
	}
}
