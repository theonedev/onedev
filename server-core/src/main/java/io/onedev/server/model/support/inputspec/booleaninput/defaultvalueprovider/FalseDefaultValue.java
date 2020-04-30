package io.onedev.server.model.support.inputspec.booleaninput.defaultvalueprovider;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200, name="false")
public class FalseDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean getDefaultValue() {
		return false;
	}

}
