package io.onedev.server.model.support.issue.field.spec;

import java.util.List;
import java.util.Map;

import io.onedev.server.buildspecmodel.inputspec.IssueChoiceInput;
import io.onedev.server.annotation.Editable;

@Editable(order=1100, name=FieldSpec.ISSUE)
public class IssueChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return IssueChoiceInput.getPropertyDef(this, indexes);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return IssueChoiceInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return IssueChoiceInput.convertToStrings(value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return Long.valueOf(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}

	@Override
	protected void runScripts() {
	}

}
