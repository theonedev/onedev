package io.onedev.server.buildspecmodel.inputspec.textinput.defaultvalueprovider;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

import javax.validation.constraints.NotEmpty;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="Specified default value")
	@NotEmpty
	@OmitName
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getDefaultValue() {
		return getValue();
	}

}
