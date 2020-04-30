package io.onedev.server.buildspec.job.paramspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.support.inputspec.IssueChoiceInput;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=1100, name=ParamSpec.ISSUE)
public class IssueChoiceParam extends ParamSpec {
	
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
			return Long.parseLong(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}

}
