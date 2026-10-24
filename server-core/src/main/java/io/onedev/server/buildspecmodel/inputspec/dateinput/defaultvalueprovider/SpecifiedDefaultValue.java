package io.onedev.server.buildspecmodel.inputspec.dateinput.defaultvalueprovider;

import java.io.Serial;
import java.util.Date;

import jakarta.validation.constraints.NotNull;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	@Serial
	private static final long serialVersionUID = 1L;

	private Date value;

	@Editable(name="Specified default value")
	@NotNull(message="may not be empty")
	@OmitName
	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	@Override
	public Date getDefaultValue() {
		return getValue();
	}

}
