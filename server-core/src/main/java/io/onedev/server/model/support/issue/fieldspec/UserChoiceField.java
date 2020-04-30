package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.AllUsers;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable(order=150, name=FieldSpec.USER)
public class UserChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new AllUsers();

	private DefaultValueProvider defaultValueProvider;
	
	private DefaultMultiValueProvider defaultMultiValueProvider;
	
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
	@ShowCondition("isDefaultValueProviderVisible")
	@NameOfEmptyValue("No default value")
	@Valid
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@SuppressWarnings("unused")
	private static boolean isDefaultValueProviderVisible() {
		return EditContext.get().getInputValue("allowMultiple").equals(false);
	}
	
	@ShowCondition("isDefaultMultiValueProviderVisible")
	@Editable(order=1100, name="Default Value")
	@NameOfEmptyValue("No default value")
	@Valid
	public DefaultMultiValueProvider getDefaultMultiValueProvider() {
		return defaultMultiValueProvider;
	}

	public void setDefaultMultiValueProvider(DefaultMultiValueProvider defaultMultiValueProvider) {
		this.defaultMultiValueProvider = defaultMultiValueProvider;
	}

	@SuppressWarnings("unused")
	private static boolean isDefaultMultiValueProviderVisible() {
		return EditContext.get().getInputValue("allowMultiple").equals(true);
	}
	
	@Override
	public List<String> getPossibleValues() {
		return OneDev.getInstance(UserManager.class).query().stream().map(user->user.getName()).collect(Collectors.toList());
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return UserChoiceInput.getPropertyDef(this, indexes, choiceProvider, defaultValueProvider, defaultMultiValueProvider);
	}

	public void onRenameUser(DefaultValueProvider defaultValueProvider, String oldName, String newName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(oldName))
				specifiedDefaultValue.setValue(newName);
		}
	}

	public Usage onDeleteUser(DefaultValueProvider defaultValueProvider, String userName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(userName))
				defaultValueProvider = null;
		}
		return new Usage();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return UserChoiceInput.convertToObject(this, strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return UserChoiceInput.convertToStrings(this, value);
	}

}
