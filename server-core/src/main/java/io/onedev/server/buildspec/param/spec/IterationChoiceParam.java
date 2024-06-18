package io.onedev.server.buildspec.param.spec;

import java.util.List;
import java.util.Map;

import io.onedev.server.buildspecmodel.inputspec.IterationChoiceInput;
import io.onedev.server.annotation.Editable;

@Editable(order=1110, name=ParamSpec.ITERATION)
public class IterationChoiceParam extends ParamSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return IterationChoiceInput.getPropertyDef(this, indexes);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return IterationChoiceInput.convertToObject(this, strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return IterationChoiceInput.convertToStrings(this, value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return IterationChoiceInput.getOrdinal(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}

}
