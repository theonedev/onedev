package io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultmultivalueprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultMultiValueProvider extends Serializable {
	
	List<String> getDefaultValue();
	
}
