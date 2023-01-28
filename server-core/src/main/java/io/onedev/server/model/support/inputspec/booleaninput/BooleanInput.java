package io.onedev.server.model.support.inputspec.booleaninput;

import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.google.common.collect.Lists;

import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.booleaninput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.util.TextUtils;

public class BooleanInput {

	public static List<String> getPossibleValues() {
		return Lists.newArrayList(TextUtils.getDisplayValue(true), TextUtils.getDisplayValue(false));
	}
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			DefaultValueProvider defaultValueProvider) {
		int index = indexes.get(inputSpec.getName());
		
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "Boolean");
		inputSpec.appendCommonAnnotations(buffer, index);
		buffer.append("    @NotNull(message=\"May not be empty\")\n");
		inputSpec.appendMethods(buffer, index, "Boolean", null, defaultValueProvider);
		
		return buffer.toString();
	}

	public static Object convertToObject(List<String> strings) {
		if (strings.size() == 0) {
			return false;
		} else if (strings.size() == 1) {
			String string = strings.iterator().next();
			if (string.equalsIgnoreCase("true") || string.equalsIgnoreCase(TextUtils.getDisplayValue(true)))
				return true;
			else if (string.equalsIgnoreCase("false") || string.equalsIgnoreCase(TextUtils.getDisplayValue(false)))
				return false;
			else
				throw new ValidationException("Invalid boolean value");
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	public static List<String> convertToStrings(Object value) {
		if (value instanceof Boolean)
			return Lists.newArrayList(TextUtils.getDisplayValue((Boolean)value));
		else
			return Lists.newArrayList(TextUtils.getDisplayValue(false));
	}

}
