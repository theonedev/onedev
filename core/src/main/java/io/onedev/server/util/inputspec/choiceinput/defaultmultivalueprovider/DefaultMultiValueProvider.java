package io.onedev.server.util.inputspec.choiceinput.defaultmultivalueprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface DefaultMultiValueProvider extends Serializable {
	
	List<String> getDefaultValue();
	
}
