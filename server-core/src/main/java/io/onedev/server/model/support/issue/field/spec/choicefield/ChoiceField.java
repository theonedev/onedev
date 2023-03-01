package io.onedev.server.model.support.issue.field.spec.choicefield;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.buildspecmodel.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.choicefield.defaultmultivalueprovider.DefaultMultiValueProvider;
import io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;

@Editable(order=145, name= FieldSpec.ENUMERATION)
public class ChoiceField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new SpecifiedChoices();

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

	@ShowCondition("isDefaultValueProviderVisible")
	@Editable(order=1100, name="Default Value", placeholder="No default value")
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
		return ChoiceInput.getPossibleValues(choiceProvider);
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return ChoiceInput.getPropertyDef(this, indexes, choiceProvider, defaultValueProvider, defaultMultiValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return ChoiceInput.convertToObject(this, strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return ChoiceInput.convertToStrings(this, value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null) {
			List<String> choices = new ArrayList<>(getChoiceProvider().getChoices(true).keySet());
			return choices.indexOf(fieldValue);
		} else {
			return super.getOrdinal(fieldValue);
		}
	}

	@Override
	protected void runScripts() {
		if (isAllowMultiple() && getDefaultMultiValueProvider() != null)
			getDefaultMultiValueProvider().getDefaultValue();
		if (!isAllowMultiple() && getDefaultValueProvider() != null)
			getDefaultValueProvider().getDefaultValue();
		getChoiceProvider().getChoices(true);
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
		super.onMoveProject(oldPath, newPath);
		choiceProvider.onMoveProject(oldPath, newPath);
	}

	@Override
	protected void onDeleteProject(Usage usage, String projectPath) {
		usage.add(choiceProvider.onDeleteProject(projectPath));
	}
	
}
