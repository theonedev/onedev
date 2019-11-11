package io.onedev.server.web.component.issue.workflowreconcile;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UndefinedFieldValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String fieldName;
	
	private final String fieldValue;
	
	public UndefinedFieldValue(String fieldName, String fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getFieldValue() {
		return fieldValue;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof UndefinedFieldValue) {
			UndefinedFieldValue otherValue = (UndefinedFieldValue) other;
			return new EqualsBuilder()
					.append(getFieldName(), otherValue.getFieldName())
					.append(getFieldValue(), otherValue.getFieldValue())
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getFieldName())
				.append(getFieldValue())
				.toHashCode();
	}
	
}
