package io.onedev.server.util.inputspec.multichoiceinput.defaultvalueprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	List<String> getDefaultValue();
	
}
