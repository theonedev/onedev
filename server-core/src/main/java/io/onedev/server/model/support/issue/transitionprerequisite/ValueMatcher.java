package io.onedev.server.model.support.issue.transitionprerequisite;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueMatcher extends Serializable {

	boolean matches(String value);
	
}
