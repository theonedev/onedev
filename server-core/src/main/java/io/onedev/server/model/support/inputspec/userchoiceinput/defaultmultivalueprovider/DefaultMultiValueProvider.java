package io.onedev.server.model.support.inputspec.userchoiceinput.defaultmultivalueprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultMultiValueProvider extends Serializable {
	
	List<String> getDefaultValue();
	
}
