package io.onedev.server.model.support.inputspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import com.google.common.collect.Lists;

public class BuildChoiceInput {
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "Long");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty()) {
			if (inputSpec.isAllowMultiple())
				buffer.append("    @Size(min=1, message=\"At least one option needs to be selected\")\n");
			else
				buffer.append("    @NotNull\n");
		}
		buffer.append("    @BuildChoice\n");
		if (inputSpec.isAllowMultiple())
			inputSpec.appendMethods(buffer, index, "List<Long>", null, null);
		else 
			inputSpec.appendMethods(buffer, index, "Long", null, null);
		
		return buffer.toString();
	}

	public static Object convertToObject(InputSpec inputSpec, List<String> strings) {
		try {
			if (inputSpec.isAllowMultiple()) 
				return strings.stream().map(it->Long.valueOf(it)).collect(Collectors.toList());
			else if (strings.size() == 0) 
				return null;
			else  
				return Long.valueOf(strings.iterator().next());
		} catch (NumberFormatException e) {
			throw new ValidationException("Invalid build number");
		}
	}

	public static List<String> convertToStrings(InputSpec inputSpec, Object value) {
		if (value instanceof List)
			return ((List<?>)value).stream().map(it->it.toString()).collect(Collectors.toList());
		else if (value instanceof Long)
			return Lists.newArrayList(value.toString());
		else
			return new ArrayList<>();
	}

}
