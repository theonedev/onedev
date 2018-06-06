package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
	
}
