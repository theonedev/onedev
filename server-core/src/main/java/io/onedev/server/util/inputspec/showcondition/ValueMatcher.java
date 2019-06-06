package io.onedev.server.util.inputspec.showcondition;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueMatcher extends Serializable {
	
	boolean matches(String value);
	
}
