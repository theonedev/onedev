package io.onedev.server.model.support.inputspec.choiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.validation.Validator;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.choiceinput.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider.DefaultValueProvider;

public class ChoiceInput {
	
	public static List<String> getPossibleValues(ChoiceProvider choiceProvider) {
		List<String> possibleValues = new ArrayList<>();
		if (OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			possibleValues.addAll(choiceProvider.getChoices(true).keySet());
		return possibleValues;
	}

	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			ChoiceProvider choiceProvider, DefaultValueProvider defaultValueProvider, 
			DefaultMultiValueProvider defaultMultiValueProvider) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, inputSpec.isAllowMultiple()? "List<String>": "String");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty()) {
			if (inputSpec.isAllowMultiple())
				buffer.append("    @Size(min=1, message=\"At least one option needs to be selected\")\n");
			else
				buffer.append("    @NotEmpty\n");
		}
		inputSpec.appendChoiceProvider(buffer, index, "@ChoiceProvider");
		
		if (inputSpec.isAllowMultiple())
			inputSpec.appendMethods(buffer, index, "List<String>", choiceProvider, defaultMultiValueProvider);
		else 
			inputSpec.appendMethods(buffer, index, "String", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	public static Object convertToObject(InputSpec inputSpec, List<String> strings) {
		if (inputSpec.isAllowMultiple()) {
			List<String> possibleValues = inputSpec.getPossibleValues();
			if (!possibleValues.isEmpty()) {
				List<String> copyOfStrings = new ArrayList<>(strings);
				copyOfStrings.removeAll(possibleValues);
				if (!copyOfStrings.isEmpty())
					throw new ValidationException("Invalid choice values: " + copyOfStrings);
				else
					return strings;
			} else {
				return strings;
			}
		} else if (strings.size() == 0) {
			return null;
		} else if (strings.size() == 1) {
			String value = strings.iterator().next();
			List<String> possibleValues = inputSpec.getPossibleValues();
			if (!possibleValues.isEmpty()) {
				if (!possibleValues.contains(value))
					throw new ValidationException("Invalid choice value");
				else
					return value;
			} else {
				return value;
			}
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> convertToStrings(InputSpec inputSpec, Object value) {
		List<String> strings = new ArrayList<>();
		if (inputSpec.isAllowMultiple()) {
			if (inputSpec.checkListElements(value, String.class))
				strings.addAll((List<String>) value);
			Collections.sort(strings);
		} else if (value instanceof String) {
			strings.add((String) value);
		} 
		return strings;
	}

}
