package io.onedev.server.util.inputspec.groupchoiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.util.Usage;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.groupchoiceinput.choiceprovider.AllGroups;
import io.onedev.server.util.inputspec.groupchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.groupchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.inputspec.groupchoiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=160, name=InputSpec.GROUP)
public class GroupChoiceInput extends InputSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new AllGroups();

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Available Choices")
	@NotNull(message="may not be empty")
	public ChoiceProvider getChoiceProvider() {
		return choiceProvider;
	}

	public void setChoiceProvider(ChoiceProvider choiceProvider) {
		this.choiceProvider = choiceProvider;
	}

	@Editable(order=1100, name="Default Value")
	@NameOfEmptyValue("No default value")
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public List<String> getPossibleValues() {
		List<String> possibleValues = new ArrayList<>();
		if (OneDev.getInstance(Validator.class).validate(getChoiceProvider()).isEmpty()) {
			for (GroupFacade group: getChoiceProvider().getChoices(true))
				possibleValues.add(group.getName());
		}
		return possibleValues;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		appendChoiceProvider(buffer, index, "@GroupChoice");
		appendMethods(buffer, index, "String", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (strings.size() == 0) 
			return null;
		else if (strings.size() == 1) 
			return strings.iterator().next();
		else 
			throw new ValidationException("Not eligible for multi-value");
	}

	@Override
	public List<String> convertToStrings(Object value) {
		if (value instanceof String)
			return Lists.newArrayList((String) value);
		else
			return new ArrayList<>();
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(oldName))
				specifiedDefaultValue.setValue(newName);
		}
	}

	@Override
	public Usage onDeleteGroup(String groupName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(groupName))
				defaultValueProvider = null;
		}
		return new Usage();
	}
	
}
