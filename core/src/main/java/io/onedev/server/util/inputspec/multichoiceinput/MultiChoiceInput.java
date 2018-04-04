package io.onedev.server.util.inputspec.multichoiceinput;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.multichoiceinput.defaultvalueprovider.DefaultValueProvider;

@Editable(order=146, name=InputSpec.MULTI_CHOICE)
public class MultiChoiceInput extends InputSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new SpecifiedChoices();

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
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "List<String>");
		appendAnnotations(buffer, index, "Size(min=1, max=100)", "ChoiceProvider", defaultValueProvider!=null);
		appendMethods(buffer, index, "List<String>", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return strings;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> convertToStrings(Object value) {
		return (List<String>) value;
	}

}
