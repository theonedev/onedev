package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.util.inputspec.InputSpec;

public class IssueField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	@XStreamOmitField
	private final String type;
	
	private final List<String> values;
	
	public IssueField(String name, String type, List<String> values) {
		this.name = name;
		this.type = type;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<String> getValues() {
		return values;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof IssueField) {
			IssueField otherField = (IssueField) other;
			return new EqualsBuilder()
					.append(getName(), otherField.getName())
					.append(getType(), otherField.getType())
					.append(getValues(), otherField.getValues())
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getName())
				.append(getType())
				.append(getValues())
				.toHashCode();
	}
	
	public boolean isVisible(Issue issue) {
		return isVisible(issue, new HashSet<>());
	}
	
	private boolean isVisible(Issue issue, Set<String> checkedFieldNames) {
		if (!checkedFieldNames.contains(getName())) {
			checkedFieldNames.add(getName());
			
			IssueWorkflow workflow = issue.getProject().getIssueWorkflow();
			InputSpec fieldSpec = workflow.getFieldSpec(getName());
			if (fieldSpec != null) {
				if (fieldSpec.getShowCondition() != null) {
					IssueField dependentField = issue.getEffectiveFields().get(fieldSpec.getShowCondition().getInputName());
					if (dependentField != null) {
						if (!dependentField.isVisible(issue, checkedFieldNames))
							return false;
						String value;
						if (!dependentField.getValues().isEmpty())
							value = dependentField.getValues().iterator().next();
						else
							value = null;
						return fieldSpec.getShowCondition().getValueMatcher().matches(value);
					} else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
