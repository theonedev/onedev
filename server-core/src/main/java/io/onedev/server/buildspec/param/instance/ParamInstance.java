package io.onedev.server.buildspec.param.instance;

import io.onedev.server.annotation.Editable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable
public class ParamInstance implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private boolean secret;
	
	private ValueProvider valueProvider = new SpecifiedValue();
	
	@Editable
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable
	@NotNull
	@Valid
	public ValueProvider getValueProvider() {
		return valueProvider;
	}

	public void setValueProvider(ValueProvider valueProvider) {
		this.valueProvider = valueProvider;
	}

	@Editable
	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ParamInstance)) 
			return false;
		if (this == other)
			return true;
		ParamInstance otherParamValue = (ParamInstance) other;
		return new EqualsBuilder()
				.append(name, otherParamValue.name)
				.append(secret, otherParamValue.secret)
				.append(valueProvider, otherParamValue.valueProvider)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(name)
			.append(valueProvider)
			.toHashCode();
	}		
	
}
