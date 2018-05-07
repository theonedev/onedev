package io.onedev.server.model.support.issue.workflow.transitionprerequisite;

import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=200, name="Is not set")
public class ValueIsNotSet implements ValueSpecification {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(List<String> values) {
		return values.isEmpty() || values.iterator().next() == null;
	}

}
