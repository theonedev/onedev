package io.onedev.server.model.support.inputspec.userchoiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultvalueprovider.DefaultValueProvider;

public class UserChoiceInput {
	
	public static List<String> getPossibleValues() {
		return OneDev.getInstance(UserManager.class).query().stream().map(user->user.getName()).collect(Collectors.toList());
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
		inputSpec.appendChoiceProvider(buffer, index, "@UserChoice");
		
		if (inputSpec.isAllowMultiple())
			inputSpec.appendMethods(buffer, index, "List<String>", choiceProvider, defaultMultiValueProvider);
		else 
			inputSpec.appendMethods(buffer, index, "String", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	public static Object convertToObject(InputSpec inputSpec, List<String> strings) {
		if (inputSpec.isAllowMultiple()) 
			return strings;
		else if (strings.size() == 0) 
			return null;
		else  
			return strings.iterator().next();
	}

	@SuppressWarnings("unchecked")
	public static List<String> convertToStrings(InputSpec inputSpec, Object value) {
		if (value instanceof List)
			return (List<String>) value;
		else if (value instanceof String)
			return Lists.newArrayList((String) value);
		else
			return new ArrayList<>();
	}
	
}
