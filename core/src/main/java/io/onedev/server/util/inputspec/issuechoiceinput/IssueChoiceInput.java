package io.onedev.server.util.inputspec.issuechoiceinput;

import java.util.List;
import java.util.Map;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.inputspec.InputSpec;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable(order=160, name=InputSpec.ISSUE_CHOICE)
public class IssueChoiceInput extends InputSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "Long");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotNull\n");
		buffer.append("    @IssueChoice\n");
		appendMethods(buffer, index, "Long", null, null);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return Long.valueOf(strings.iterator().next());
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList(value.toString());
	}

	@Override
	public long getOrdinal(OneContext context, Object fieldValue) {
		if (fieldValue != null)
			return (Long) fieldValue;
		else
			return super.getOrdinal(context, fieldValue);
	}

}
