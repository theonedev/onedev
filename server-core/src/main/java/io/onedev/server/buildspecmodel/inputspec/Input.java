package io.onedev.server.buildspecmodel.inputspec;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.validation.ValidationException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.model.support.issue.field.spec.FieldSpec;

/*
 * This file is left here for backward compatibility (OD-2531/OD-2532)
 */
public class Input implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Input.class);

	private final String name;
	
	private final String type;
	
	private final List<String> values;
	
	public Input(String name, String type, List<String> values) {
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
		} else if (other instanceof Input) {
			Input otherField = (Input) other;
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

	@Nullable
	public Object getTypedValue(@Nullable InputSpec inputSpec) {
		if (inputSpec != null) {
			try {
				return inputSpec.convertToObject(getValues());
			} catch (ValidationException e) {
				String displayValue;
				if (type.equals(FieldSpec.SECRET)) 
					displayValue = SecretInput.MASK;
				else 
					displayValue = "" + getValues();
				
				logger.error("Error converting input (name: {}, value: {}, error: {})", 
						getName(), displayValue, e.getMessage());
				return null;
			}
		} else {
			return null;
		}
	}
	
}
