package io.onedev.server.model.support.inputspec.workingperiodinput.defaultvalueprovider;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	Integer getDefaultValue();
	
}
