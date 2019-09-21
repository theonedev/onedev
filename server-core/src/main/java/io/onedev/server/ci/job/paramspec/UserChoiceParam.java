package io.onedev.server.ci.job.paramspec;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.IssueReaders;
import io.onedev.server.model.support.inputspec.userchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=150, name=ParamSpec.USER)
public class UserChoiceParam extends ParamSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new IssueReaders();

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
		return OneDev.getInstance(UserManager.class).query().stream().map(user->user.getName()).collect(Collectors.toList());
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return UserChoiceInput.getPropertyDef(this, indexes, choiceProvider, defaultValueProvider);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return UserChoiceInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return UserChoiceInput.convertToStrings(value);
	}

}
