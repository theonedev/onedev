package io.onedev.server.buildspecmodel.inputspec.showcondition;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable
public interface ValueMatcher extends Serializable {
	
	boolean matches(List<String> values);
	
}
