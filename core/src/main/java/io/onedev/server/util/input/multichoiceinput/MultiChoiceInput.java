package io.onedev.server.util.input.multichoiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.input.choiceprovider.ChoiceProvider;
import io.onedev.server.util.input.multichoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.utils.StringUtils;

@Editable(order=146, name=Input.MULTI_CHOICE)
public class MultiChoiceInput extends Input {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new SpecifiedChoices();

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Available Choices")
	@NotNull
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
		if (OneDev.getInstance(Validator.class).validate(getChoiceProvider()).isEmpty())
			return getChoiceProvider().getChoices(true);
		else
			return new ArrayList<>();
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
	public Object toObject(String string) {
		return StringUtils.split(string);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value) {
		return StringUtils.join((List<String>)value);
	}

}
