package io.onedev.server.security.permission;

import io.onedev.server.model.LinkSpec;
import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;

import org.jspecify.annotations.Nullable;

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
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
