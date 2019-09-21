package io.onedev.server.model.support.inputspec.numberinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.google.common.collect.Lists;

import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.numberinput.defaultvalueprovider.DefaultValueProvider;

public class NumberInput {
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			Integer minValue, Integer maxValue, DefaultValueProvider defaultValueProvider) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "Integer");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty())
			buffer.append("    @NotNull(message=\"May not be empty\")\n");
		if (minValue != null) {
			if (maxValue != null) {
				buffer.append("    @Range(min=" + minValue.toString() + "L,max=" + maxValue.toString() +"L)\n");
			} else {
				buffer.append("    @Range(min=" + minValue.toString() + "L)\n");
			}
		} else if (maxValue != null) {
			buffer.append("    @Range(max=" + maxValue.toString() + "L)\n");
		}
		inputSpec.appendMethods(buffer, index, "Integer", null, defaultValueProvider);
		
		return buffer.toString();
	}

	public static Object convertToObject(List<String> strings) {
		if (strings.size() == 0) { 
			return null;
		} else if (strings.size() == 1) {
			try {
				return Integer.valueOf(strings.iterator().next());
			} catch (NumberFormatException e) {
				throw new ValidationException("Invalid number value");
			}
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	public static List<String> convertToStrings(Object value) {
		if (value instanceof Integer)
			return Lists.newArrayList(String.valueOf(value));
		else
			return new ArrayList<>();
	}

}
