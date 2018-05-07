package io.onedev.server.model.support.issue.workflow.transitionprerequisite;

import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=100, name="Is set")
public class ValueIsSet implements ValueSpecification {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(List<String> values) {
		return !values.isEmpty() && values.iterator().next() != null;
	}

}
