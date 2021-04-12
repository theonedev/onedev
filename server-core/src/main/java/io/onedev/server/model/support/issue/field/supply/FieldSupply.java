package io.onedev.server.model.support.issue.field.supply;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class FieldSupply implements Serializable {

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
		if (!(other instanceof FieldSupply)) 
			return false;
		if (this == other)
			return true;
		FieldSupply otherIssueField = (FieldSupply) other;
		return new EqualsBuilder()
			.append(name, otherIssueField.name)
			.append(valueProvider, otherIssueField.valueProvider)
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
