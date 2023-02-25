package io.onedev.server.buildspecmodel.inputspec.dateinput.defaultvalueprovider;

import java.io.Serializable;
import java.util.Date;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	Date getDefaultValue();
	
}
