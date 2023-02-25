package io.onedev.server.buildspecmodel.inputspec.textinput;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.ValidationException;

import com.google.common.collect.Lists;

import io.onedev.server.buildspecmodel.inputspec.textinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;

public class TextInput {

	// MySQL indexable field max length
	public static final int MAX_LEN = 500;
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			@Nullable String pattern, boolean multiline, DefaultValueProvider defaultValueProvider) {
		if (pattern != null)
			pattern = InputSpec.escape(pattern);
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "String");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		if (multiline)
			buffer.append("    @Multiline\n");
		buffer.append(MessageFormat.format("@Size(max={0}, message=\"Text is too long. Max {0} characters\")", MAX_LEN));
		if (pattern != null)
			buffer.append("    @Pattern(regexp=\"" + pattern + "\", message=\"Should match regular expression: " + pattern + "\")\n");
		inputSpec.appendMethods(buffer, index, "String", null, defaultValueProvider);

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
