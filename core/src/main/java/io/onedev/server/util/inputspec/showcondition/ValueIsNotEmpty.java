package io.onedev.server.util.inputspec.showcondition;

import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=300, name="is not empty")
public class ValueIsNotEmpty implements ValueMatcher {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean matches(String value) {
		return value != null;
	}
	
}
