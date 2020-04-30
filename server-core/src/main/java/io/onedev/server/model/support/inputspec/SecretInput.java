package io.onedev.server.model.support.inputspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.google.common.collect.Lists;

public class SecretInput {

	public static final String MASK = "*****";
	
	public static final String LITERAL_VALUE_PREFIX = "$OneDev-Secret-Literal$";
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "String");
		inputSpec.appendCommonAnnotations(buffer, index);
		buffer.append("    @NotEmpty\n");
		buffer.append("    @Password\n");
		inputSpec.appendMethods(buffer, index, "String", null, null);
		
		return buffer.toString();
	}

	public static Object convertToObject(List<String> strings) {
		if (strings.size() == 0)
			return null;
		else if (strings.size() == 1)
			return strings.iterator().next();
		else
			throw new ValidationException("Not eligible for multi-value");
	}

	public static List<String> convertToStrings(Object value) {
		if (value instanceof String)
			return Lists.newArrayList((String)value);
		else
			return new ArrayList<>();
	}

}
