package io.onedev.server.model.support.inputspec.groupchoiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.validation.Validator;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.model.Group;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.groupchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.groupchoiceinput.defaultvalueprovider.DefaultValueProvider;

public class GroupChoiceInput {
	
	public static List<String> getPossibleValues(ChoiceProvider choiceProvider) {
		List<String> possibleValues = new ArrayList<>();
		if (OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty()) {
			for (Group group: choiceProvider.getChoices(true))
				possibleValues.add(group.getName());
		}
		return possibleValues;
	}

	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			ChoiceProvider choiceProvider, DefaultValueProvider defaultValueProvider) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "String");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		inputSpec.appendChoiceProvider(buffer, index, "@GroupChoice");
		inputSpec.appendMethods(buffer, index, "String", choiceProvider, defaultValueProvider);
		
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
			return Lists.newArrayList((String) value);
		else
			return new ArrayList<>();
	}

}
