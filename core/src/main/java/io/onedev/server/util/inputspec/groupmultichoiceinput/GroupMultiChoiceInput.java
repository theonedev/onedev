package io.onedev.server.util.inputspec.groupmultichoiceinput;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.groupchoiceprovider.AllGroups;
import io.onedev.server.util.inputspec.groupchoiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.groupmultichoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.inputspec.groupmultichoiceinput.defaultvalueprovider.SpecifiedDefaultValue;

@Editable(order=161, name=InputSpec.GROUP_MULTI_CHOICE)
public class GroupMultiChoiceInput extends InputSpec {
	
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
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "List<String>");
		appendAnnotations(buffer, index, "Size(min=1, max=100)", "GroupChoice", defaultValueProvider!=null);
		appendMethods(buffer, index, "List<String>", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			int index = specifiedDefaultValue.getValue().indexOf(oldName);
			if (index != -1)
				specifiedDefaultValue.getValue().set(index, newName);
		}
	}

	@Override
	public List<String> onDeleteGroup(String groupName) {
		List<String> usages = super.onDeleteGroup(groupName);
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().contains(groupName))
				usages.add("Default Value");
		}
		return usages;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return strings;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> convertToStrings(Object value) {
		return (List<String>)value;
	}

}
