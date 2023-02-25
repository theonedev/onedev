package io.onedev.server.buildspecmodel.inputspec.integerinput.defaultvalueprovider;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	int getDefaultValue();
	
}
