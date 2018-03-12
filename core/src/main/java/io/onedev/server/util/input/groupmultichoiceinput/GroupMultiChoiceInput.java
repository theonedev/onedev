package io.onedev.server.util.input.groupmultichoiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.groupchoiceprovider.AllGroups;
import io.onedev.server.util.input.groupchoiceprovider.ChoiceProvider;
import io.onedev.server.util.input.groupmultichoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.utils.StringUtils;

@Editable(order=161, name=Input.GROUP_MULTI_CHOICE)
public class GroupMultiChoiceInput extends Input {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new AllGroups();

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
		appendField(buffer, index, "List<String>");
		appendAnnotations(buffer, index, "Size(min=1, max=100)", "GroupChoice", defaultValueProvider!=null);
		appendMethods(buffer, index, "List<String>", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object toObject(String string) {
		return StringUtils.splitAndTrim(string);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value) {
		return StringUtils.join((List<String>)value);
	}

}
