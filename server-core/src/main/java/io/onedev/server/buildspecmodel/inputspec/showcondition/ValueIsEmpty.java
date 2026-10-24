package io.onedev.server.buildspecmodel.inputspec.showcondition;

import java.io.Serial;
import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable(order=400, name="is empty")
public class ValueIsEmpty implements ValueMatcher {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(List<String> values) {
		return values.isEmpty();
	}
	
}
