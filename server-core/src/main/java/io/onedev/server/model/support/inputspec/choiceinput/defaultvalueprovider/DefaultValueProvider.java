package io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	String getDefaultValue();
	
}
