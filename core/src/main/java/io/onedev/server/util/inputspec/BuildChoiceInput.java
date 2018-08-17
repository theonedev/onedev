package io.onedev.server.util.inputspec;

import java.util.List;
import java.util.Map;

import io.onedev.server.web.editable.annotation.Editable;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable(order=160, name=InputSpec.BUILD)
public class BuildChoiceInput extends InputSpec {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "Long");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotNull\n");
		buffer.append("    @BuildChoice\n");
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
	public long getOrdinal(Object fieldValue) {
		if (fieldValue != null)
			return (Long) fieldValue;
		else
			return super.getOrdinal(fieldValue);
	}

}
