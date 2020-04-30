package io.onedev.server.buildspec.job.paramspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.support.inputspec.CommitInput;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=1400, name=ParamSpec.COMMIT)
public class CommitParam extends ParamSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return CommitInput.getPropertyDef(this, indexes);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}
	
	@Override
	public Object convertToObject(List<String> strings) {
		return CommitInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return CommitInput.convertToStrings(value);
	}
	
}
