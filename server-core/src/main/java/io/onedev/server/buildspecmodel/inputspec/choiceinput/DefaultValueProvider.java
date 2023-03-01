package io.onedev.server.buildspecmodel.inputspec.choiceinput;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	String getDefaultValue();
	
}
