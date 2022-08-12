package io.onedev.server.buildspec.param.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.AllUsers;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable(order=150, name=ParamSpec.USER)
public class UserChoiceParam extends ParamSpec {
	
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

	@Editable(order=1100, name="Default Value", placeholder="No default value")
	@ShowCondition("isDefaultValueProviderVisible")
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
	@Editable(order=1100, name="Default Value", placeholder="No default value")
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
		return UserChoiceInput.getPossibleValues();
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return UserChoiceInput.getPropertyDef(this, indexes, choiceProvider, defaultValueProvider, defaultMultiValueProvider);
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
