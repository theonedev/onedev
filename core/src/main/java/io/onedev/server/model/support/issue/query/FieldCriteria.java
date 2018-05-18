package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;

public abstract class FieldCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	protected long getFieldOrdinal(Issue issue) {
		InputSpec field = issue.getProject().getIssueWorkflow().getField(fieldName);
		if (field != null) {
			return field.getOrdinal(new OneContext() {

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
		for (IssueField field: issue.getFields()) {
			if (field.getName().equals(fieldName))
				strings.add(field.getValue());
		}
		if (strings.isEmpty() || strings.contains(null)) {
			return null;
		} else {
			InputSpec field = issue.getProject().getIssueWorkflow().getField(fieldName);
			if (field != null)
				return field.convertToObject(strings);
			else
				return null;
		}
	}
	
	protected Object getFieldValue(Issue issue) {
		return getFieldValue(issue, fieldName);
	}
	
}
