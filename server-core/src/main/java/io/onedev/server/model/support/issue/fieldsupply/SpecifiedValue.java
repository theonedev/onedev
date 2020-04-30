package io.onedev.server.model.support.issue.fieldsupply;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SpecifiedValue implements ValueProvider {

	private static final long serialVersionUID = 1L;

	public static final String DISPLAY_NAME = "Use specified value";
	
	public static final String SECRET_DISPLAY_NAME = "Use specified job secret";

	private List<String> value = new ArrayList<>();
	
	@Override
	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SpecifiedValue)) 
			return false;
		if (this == other)
			return true;
		SpecifiedValue otherSpecifiedValue = (SpecifiedValue) other;
		return new EqualsBuilder()
			.append(value, otherSpecifiedValue.value)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(value)
			.toHashCode();
	}

}
