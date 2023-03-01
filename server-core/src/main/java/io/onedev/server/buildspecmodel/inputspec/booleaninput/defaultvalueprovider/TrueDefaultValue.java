package io.onedev.server.buildspecmodel.inputspec.booleaninput.defaultvalueprovider;

import io.onedev.server.annotation.Editable;

@Editable(order=100, name="true")
public class TrueDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean getDefaultValue() {
		return true;
	}

}
