package io.onedev.server.util.inputspec.choiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.choiceinput.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.util.inputspec.choiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable(order=145, name=InputSpec.ENUMERATION)
public class ChoiceInput extends InputSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new SpecifiedChoices();

	private DefaultValueProvider defaultValueProvider;
	
	private DefaultMultiValueProvider defaultMultiValueProvider;
	
	@Editable(order=1000, name="Available Choices")
	@NotNull(message="may not be empty")
	public ChoiceProvider getChoiceProvider() {
		return choiceProvider;
	}

	public void setChoiceProvider(ChoiceProvider choiceProvider) {
		this.choiceProvider = choiceProvider;
	}

	@ShowCondition("isDefaultValueProviderVisible")
	@Editable(order=1100, name="Default Value")
	@NameOfEmptyValue("No default value")
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}
	
	@SuppressWarnings("unused")
	private static boolean isDefaultValueProviderVisible() {
		return OneContext.get().getEditContext().getInputValue("allowMultiple").equals(false);
	}

	@ShowCondition("isDefaultMultiValueProviderVisible")
	@Editable(order=1100, name="Default Value")
	@NameOfEmptyValue("No default value")
	public DefaultMultiValueProvider getDefaultMultiValueProvider() {
		return defaultMultiValueProvider;
	}

	public void setDefaultMultiValueProvider(DefaultMultiValueProvider defaultMultiValueProvider) {
		this.defaultMultiValueProvider = defaultMultiValueProvider;
	}

	@SuppressWarnings("unused")
	private static boolean isDefaultMultiValueProviderVisible() {
		return OneContext.get().getEditContext().getInputValue("allowMultiple").equals(true);
	}
	
	@Override
	public List<String> getPossibleValues() {
		List<String> possibleValues = new ArrayList<>();
		if (OneDev.getInstance(Validator.class).validate(getChoiceProvider()).isEmpty())
			possibleValues.addAll(getChoiceProvider().getChoices(true).keySet());
		return possibleValues;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, isAllowMultiple()? "List<String>": "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty()) {
			if (isAllowMultiple())
				buffer.append("    @Size(min=1, message=\"At least one option needs to be selected\")\n");
			else
				buffer.append("    @NotEmpty\n");
		}
		appendChoiceProvider(buffer, index, "@ChoiceProvider");
		
		if (isAllowMultiple())
			appendMethods(buffer, index, "List<String>", choiceProvider, defaultMultiValueProvider);
		else 
			appendMethods(buffer, index, "String", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (isAllowMultiple())
			return strings;
		else
			return strings.iterator().next();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> convertToStrings(Object value) {
		if (isAllowMultiple())
			return (List<String>) value;
		else
			return Lists.newArrayList((String) value);
	}

	@Override
	public long getOrdinal(Object fieldValue) {
		if (fieldValue != null) {
			List<String> choices = new ArrayList<>(getChoiceProvider().getChoices(true).keySet());
			return choices.indexOf(fieldValue);
		} else {
			return super.getOrdinal(fieldValue);
		}
	}

}
