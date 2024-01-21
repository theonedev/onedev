package io.onedev.server.model.support.issue.field.spec.userchoicefield;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.buildspecmodel.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider.AllUsers;
import io.onedev.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultmultivalueprovider.SpecifiedDefaultMultiValue;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Editable(order=150, name= FieldSpec.USER)
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
	
	private static boolean isSubscriptionActive() {
		return OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive();
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
	public void onRenameUser(String oldName, String newName) {
		if (!isAllowMultiple() && defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			for (var defaultValue: specifiedDefaultValue.getDefaultValues()) {
				if (defaultValue.getValue().equals(oldName))
					defaultValue.setValue(newName);					
			}
		}
		if (isAllowMultiple() && defaultMultiValueProvider instanceof SpecifiedDefaultMultiValue) {
			SpecifiedDefaultMultiValue specifiedDefaultMultiValue = (SpecifiedDefaultMultiValue) defaultMultiValueProvider;
			for (var defaultValue: specifiedDefaultMultiValue.getDefaultValues()) {
				int index = defaultValue.getValue().indexOf(oldName);
				if (index != -1)
					defaultValue.getValue().set(index, newName);
			}
		}
	}

	@Override
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (!isAllowMultiple() && defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			int index = 0;
			for (var defaultValue: specifiedDefaultValue.getDefaultValues()) {
				if (defaultValue.getValue().equals(userName))
					usage.add("default value: item #" + index);
				index++;
			}
		}
		if (isAllowMultiple() && defaultMultiValueProvider instanceof SpecifiedDefaultMultiValue) {
			SpecifiedDefaultMultiValue specifiedDefaultMultiValueValue = (SpecifiedDefaultMultiValue) defaultMultiValueProvider;
			int index = 0;
			for (var defaultValue: specifiedDefaultMultiValueValue.getDefaultValues()) {
				if (defaultValue.getValue().contains(userName))
					usage.add("default value: item #" + index);
				index++;
			}
		}
		return usage.prefix("custom fields: " + getName());
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return UserChoiceInput.convertToObject(this, strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return UserChoiceInput.convertToStrings(this, value);
	}

	@Override
	protected void runScripts() {
		if (isAllowMultiple() && getDefaultMultiValueProvider() != null)
			getDefaultMultiValueProvider().getDefaultValue();
		if (!isAllowMultiple() && getDefaultValueProvider() != null)
			getDefaultValueProvider().getDefaultValue();
		getChoiceProvider().getChoices(true);
	}
}
