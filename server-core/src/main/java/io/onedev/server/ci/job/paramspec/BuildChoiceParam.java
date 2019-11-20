package io.onedev.server.ci.job.paramspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.util.inputspec.BuildChoiceInput;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=1200, name=ParamSpec.BUILD)
public class BuildChoiceParam extends ParamSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return BuildChoiceInput.getPropertyDef(this, indexes);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return BuildChoiceInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return BuildChoiceInput.convertToStrings(value);
	}

	@Override
	public long getOrdinal(Object fieldValue) {
		if (fieldValue != null)
			return (Long) fieldValue;
		else
			return super.getOrdinal(fieldValue);
	}

}
