package io.onedev.server.util.input.dateinput.defaultvalueprovider;

import java.io.Serializable;
import java.util.Date;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	Date getDefaultValue();
	
}
