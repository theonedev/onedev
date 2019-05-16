package io.onedev.server.util.inputspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import io.onedev.server.web.editable.annotation.Editable;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable(order=1200, name=InputSpec.BUILD)
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

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (strings.size() == 0) {
			return null;
		} else if (strings.size() == 1) {
			String value = strings.iterator().next();
			try {
				return Long.valueOf(value);
			} catch (NumberFormatException e) {
				throw new ValidationException("Invalid build number");
			}
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	@Override
	public List<String> convertToStrings(Object value) {
		if (value instanceof Long)
			return Lists.newArrayList((String) value);
		else
			return new ArrayList<>();
	}

	@Override
	public long getOrdinal(Object fieldValue) {
		if (fieldValue != null)
			return (Long) fieldValue;
		else
			return super.getOrdinal(fieldValue);
	}

}
