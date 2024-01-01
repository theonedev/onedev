package io.onedev.server.buildspec.param.instance;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;

@Editable
public class ParamInstances implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private boolean secret;
	
	private ValuesProvider valuesProvider = new SpecifiedValues();
	
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
	public ValuesProvider getValuesProvider() {
		return valuesProvider;
	}

	public void setValuesProvider(ValuesProvider valuesProvider) {
		this.valuesProvider = valuesProvider;
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
		if (!(other instanceof ParamInstances)) 
			return false;
		if (this == other)
			return true;
		ParamInstances otherParamValue = (ParamInstances) other;
		return new EqualsBuilder()
			.append(name, otherParamValue.name)
			.append(valuesProvider, otherParamValue.valuesProvider)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(name)
			.append(valuesProvider)
			.toHashCode();
	}		
	
}
