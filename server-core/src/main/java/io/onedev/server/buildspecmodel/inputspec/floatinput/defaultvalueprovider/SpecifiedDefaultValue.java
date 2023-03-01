package io.onedev.server.buildspecmodel.inputspec.floatinput.defaultvalueprovider;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private float value;

	@Editable(name="Specified default value")
	@OmitName
	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public float getDefaultValue() {
		return getValue();
	}

}
