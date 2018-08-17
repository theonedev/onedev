package io.onedev.server.util.inputspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.web.editable.annotation.Editable;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable(order=100, name=InputSpec.COMMIT)
public class CommitInput extends InputSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		appendMethods(buffer, index, "String", null, null);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return strings.iterator().next();
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList((String) value);
	}
	
}
