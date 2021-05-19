package io.onedev.server.security.permission;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.shiro.authz.Permission;

public class EditIssueField implements Permission {

	private final Collection<String> fields;
	
	public EditIssueField(@Nullable Collection<String> fields) {
		this.fields = fields;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof EditIssueField) {
			EditIssueField editIssueField = (EditIssueField) p;
			if (fields == null)
				return true;
			else if (editIssueField.fields == null)
				return false;
			else
				return fields.containsAll(editIssueField.fields);
		} else {
			return new AccessProject().implies(p);
		}
	}

}
