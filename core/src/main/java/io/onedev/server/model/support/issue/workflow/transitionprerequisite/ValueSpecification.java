package io.onedev.server.model.support.issue.workflow.transitionprerequisite;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface ValueSpecification extends Serializable {
	
	boolean matches(List<String> values);
	
}
