package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.model.support.inputspec.groupchoiceinput.choiceprovider.AllGroups;
import io.onedev.server.model.support.inputspec.groupchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.groupchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.model.support.inputspec.groupchoiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=160, name=FieldSpec.GROUP)
public class GroupChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new AllGroups();

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Available Choices")
	@NotNull(message="may not be empty")
	@Valid
	public ChoiceProvider getChoiceProvider() {
		return choiceProvider;
	}

	public void setChoiceProvider(ChoiceProvider choiceProvider) {
		this.choiceProvider = choiceProvider;
	}

	@Editable(order=1100, name="Default Value")
	@NameOfEmptyValue("No default value")
	@Valid
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public List<String> getPossibleValues() {
		return GroupChoiceInput.getPossibleValues(choiceProvider);
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return GroupChoiceInput.getPropertyDef(this, indexes, choiceProvider, defaultValueProvider);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return GroupChoiceInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return GroupChoiceInput.convertToStrings(value);
	}

	public void onRenameGroup(DefaultValueProvider defaultValueProvider, String oldName, String newName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(oldName))
				specifiedDefaultValue.setValue(newName);
		}
	}

	public Usage onDeleteGroup(DefaultValueProvider defaultValueProvider, String groupName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(groupName))
				defaultValueProvider = null;
		}
		return new Usage();
	}
	
}
