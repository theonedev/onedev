package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name=FieldSpec.SECRET)
public class SecretField extends FieldSpec {

	private static final long serialVersionUID = 1L;

	@Editable
	@Override
	public boolean isAllowEmpty() {
		return false;
	}
	
	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return SecretInput.getPropertyDef(this, indexes);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return SecretInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return SecretInput.convertToStrings(value);
	}

}
