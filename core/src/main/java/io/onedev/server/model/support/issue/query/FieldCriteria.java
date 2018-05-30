package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;

public abstract class FieldCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	protected long getFieldOrdinal(Issue issue) {
		InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getFieldSpec(fieldName);
		if (fieldSpec != null) {
			return fieldSpec.getOrdinal(new OneContext() {

				@Override
				public Project getProject() {
					return issue.getProject();
				}

				@Override
				public EditContext getEditContext(int level) {
					return new EditContext() {

						@Override
						public Object getInputValue(String name) {
							return getFieldValue(issue, name);
						}
						
					};
				}

				@Override
				public InputContext getInputContext() {
					throw new UnsupportedOperationException();
				}
			}, getFieldValue(issue));
			
		} else {
			return -1;
		}
	}
	
	private Object getFieldValue(Issue issue, String fieldName) {
		List<String> strings = new ArrayList<>();
		for (IssueFieldUnary field: issue.getFieldUnaries()) {
			if (field.getName().equals(fieldName))
				strings.add(field.getValue());
		}
		if (strings.isEmpty() || strings.contains(null)) {
			return null;
		} else {
			InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getFieldSpec(fieldName);
			if (fieldSpec != null)
				return fieldSpec.convertToObject(strings);
			else
				return null;
		}
	}
	
	protected Object getFieldValue(Issue issue) {
		return getFieldValue(issue, fieldName);
	}

	@Override
	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (!Issue.BUILTIN_FIELDS.containsKey(fieldName) 
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
