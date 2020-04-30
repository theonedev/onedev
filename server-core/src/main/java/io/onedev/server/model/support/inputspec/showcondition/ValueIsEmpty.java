package io.onedev.server.model.support.inputspec.showcondition;

import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=400, name="is empty")
public class ValueIsEmpty implements ValueMatcher {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(List<String> values) {
		return values.isEmpty();
	}
	
}
