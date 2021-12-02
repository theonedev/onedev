package io.onedev.server.security.permission;

import org.apache.shiro.authz.Permission;

import io.onedev.server.model.LinkSpec;

public class EditIssueLink implements Permission {

	private final LinkSpec link;
	
	public EditIssueLink(LinkSpec link) {
		this.link = link;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof EditIssueLink) {
			EditIssueLink editIssueLink = (EditIssueLink) p;
			return link.equals(editIssueLink.link);
		} else {
			return new AccessProject().implies(p);
		}
	}

}
