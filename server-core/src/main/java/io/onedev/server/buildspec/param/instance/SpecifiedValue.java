package io.onedev.server.buildspec.param.instance;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

@Editable(name="Use specified value or job secret")
public class SpecifiedValue implements ValueProvider {

	private static final long serialVersionUID = 1L;

	public static final String DISPLAY_NAME = "Use specified value";
	
	public static final String SECRET_DISPLAY_NAME = "Use specified job secret";

	private List<String> value = new ArrayList<>();
	
	@Editable
	@Interpolative
	public List<String> getValue() {
		return value;
	}
	
	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public List<String> getValue(Build build, ParamCombination paramCombination) {
		return getValue();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SpecifiedValue)) 
			return false;
		if (this == other)
			return true;
		SpecifiedValue otherSpecifiedValues = (SpecifiedValue) other;
		return new EqualsBuilder()
			.append(value, otherSpecifiedValues.value)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(value)
			.toHashCode();
	}

}
