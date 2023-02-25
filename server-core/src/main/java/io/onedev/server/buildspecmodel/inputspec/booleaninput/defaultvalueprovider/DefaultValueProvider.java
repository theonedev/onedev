package io.onedev.server.buildspecmodel.inputspec.booleaninput.defaultvalueprovider;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	boolean getDefaultValue();
	
}
