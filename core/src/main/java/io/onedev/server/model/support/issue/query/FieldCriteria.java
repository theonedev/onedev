package io.onedev.server.model.support.issue.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public abstract class FieldCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (!Issue.FIELD_PATHS.containsKey(fieldName) 
				&& project.getIssueWorkflow().getFieldSpec(fieldName) == null) {
			undefinedFields.add(fieldName);
		}
		return undefinedFields;
	}

	@Override
	public void onRenameField(String oldField, String newField) {
		if (oldField.equals(fieldName))
			fieldName = newField;
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		return fieldName.equals(this.fieldName);
	}
	
}
