package io.onedev.server.security.permission;

import java.util.Collection;

import org.apache.shiro.authz.Permission;

public class EditIssueField implements Permission {

	private final Collection<String> fields;
	
	public EditIssueField(Collection<String> fields) {
		this.fields = fields;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof EditIssueField) {
			EditIssueField editIssueField = (EditIssueField) p;
			return fields.containsAll(editIssueField.fields);
		} else {
			return new AccessProject().implies(p);
		}
	}

}
