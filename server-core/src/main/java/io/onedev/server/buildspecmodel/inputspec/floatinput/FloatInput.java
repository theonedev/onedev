package io.onedev.server.buildspecmodel.inputspec.floatinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.google.common.collect.Lists;

import io.onedev.server.buildspecmodel.inputspec.floatinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;

public class FloatInput {
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			DefaultValueProvider defaultValueProvider) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "Float");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty())
			buffer.append("    @NotNull(message=\"May not be empty\")\n");
		inputSpec.appendMethods(buffer, index, "Float", null, defaultValueProvider);
		
		return buffer.toString();
	}

	public static Object convertToObject(List<String> strings) {
		if (strings.size() == 0) { 
			return null;
		} else if (strings.size() == 1) {
			try {
				return Float.valueOf(strings.iterator().next());
			} catch (NumberFormatException e) {
				throw new ValidationException("Invalid float value");
			}
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	public static List<String> convertToStrings(Object value) {
		if (value instanceof Float)
			return Lists.newArrayList(String.valueOf(value));
		else
			return new ArrayList<>();
	}

}
