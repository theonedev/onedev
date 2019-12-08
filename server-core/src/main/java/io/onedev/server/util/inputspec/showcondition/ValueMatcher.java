package io.onedev.server.util.inputspec.showcondition;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueMatcher extends Serializable {
	
	boolean matches(List<String> values);
	
}
