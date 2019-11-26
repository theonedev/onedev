package io.onedev.server.security.permission;

import javax.annotation.Nullable;

import org.apache.shiro.authz.Permission;

public class EditIssueField implements Permission {

	private final String fieldName;
	
	public EditIssueField(@Nullable String fieldName) {
		this.fieldName = fieldName;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof EditIssueField) {
			EditIssueField editIssueField = (EditIssueField) p;
			return fieldName == null || fieldName.equals(editIssueField.fieldName);
		} else {
			return new AccessProject().implies(p);
		}
	}

}
