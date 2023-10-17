package io.onedev.server.model.support.issue.field.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider.AllGroups;
import io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.annotation.Editable;

@Editable(order=160, name=FieldSpec.GROUP)
public class GroupChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new AllGroups();

	private DefaultValueProvider defaultValueProvider;
	
	private boolean editEstimatedTime = true;
	
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

	@Editable(order=1200, name="Can Edit Estimated Time", description = "If ticked, group indicated by this " +
			"field will be able to edit estimated time of corresponding issues if time tracking is enabled")
	@ShowCondition("isSubscriptionActive")
	public boolean isEditEstimatedTime() {
		return editEstimatedTime;
	}

	public void setEditEstimatedTime(boolean editEstimatedTime) {
		this.editEstimatedTime = editEstimatedTime;
	}

	private static boolean isSubscriptionActive() {
		return OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive();
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

	@Override
	public void onRenameGroup(String oldName, String newName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(oldName))
				specifiedDefaultValue.setValue(newName);
		}
	}

	@Override
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(groupName))
				usage.add("default value");
		}
		return usage.prefix("custom fields: " + getName());
	}

	@Override
	protected void runScripts() {
		if (getDefaultValueProvider() != null)
			getDefaultValueProvider().getDefaultValue();
		getChoiceProvider().getChoices(true);
	}
	
}
