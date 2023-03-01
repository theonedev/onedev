package io.onedev.server.buildspecmodel.inputspec.showcondition;

import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable(order=300, name="is not empty")
public class ValueIsNotEmpty implements ValueMatcher {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean matches(List<String> values) {
		return !values.isEmpty();
	}
	
}
