package io.onedev.server.model.support.issue.field.spec;

import java.util.List;
import java.util.Map;

import io.onedev.server.buildspecmodel.inputspec.BuildChoiceInput;
import io.onedev.server.annotation.Editable;

@Editable(order=1200, name=FieldSpec.BUILD)
public class BuildChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return BuildChoiceInput.getPropertyDef(this, indexes);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return BuildChoiceInput.convertToObject(this, strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return BuildChoiceInput.convertToStrings(this, value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return Long.parseLong(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}

	@Override
	protected void runScripts() {
		
	}

}
