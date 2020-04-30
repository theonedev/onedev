package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.support.inputspec.PullRequestChoiceInput;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=1000, name=FieldSpec.PULLREQUEST)
public class PullRequestChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return PullRequestChoiceInput.getPropertyDef(this, indexes);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return PullRequestChoiceInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return PullRequestChoiceInput.convertToStrings(value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return Long.parseLong(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}

}
