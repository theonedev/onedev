package io.onedev.server.model.support.inputspec.booleaninput.defaultvalueprovider;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100, name="true")
public class TrueDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean getDefaultValue() {
		return true;
	}

}
