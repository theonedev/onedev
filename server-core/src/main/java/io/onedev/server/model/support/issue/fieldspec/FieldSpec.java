package io.onedev.server.model.support.issue.fieldspec;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.inputspec.showcondition.ShowCondition;
import io.onedev.server.model.support.inputspec.showcondition.ValueIsNotAnyOf;
import io.onedev.server.model.support.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.annotation.FieldName;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public abstract class FieldSpec extends InputSpec {
	
	private static final long serialVersionUID = 1L;
	
	private String nameOfEmptyValue;
	
	@Editable(order=10)
	@FieldName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Editable(order=30, description="Optionally describes the custom field")
	@NameOfEmptyValue("No description")
	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}

	@Editable(order=35, description="Whether or not multiple values can be specified for this field")
	@Override
	public boolean isAllowMultiple() {
		return super.isAllowMultiple();
	}

	@Override
	public void setAllowMultiple(boolean allowMultiple) {
		super.setAllowMultiple(allowMultiple);
	}

	@Editable(order=40, name="Show Conditionally", description="Enable if visibility of this field depends on other fields")
	@NameOfEmptyValue("Always")
	@Valid
	@Override
	public ShowCondition getShowCondition() {
		return super.getShowCondition();
	}

	@Override
	public void setShowCondition(ShowCondition showCondition) {
		super.setShowCondition(showCondition);
	}
	
	@Editable(order=50, name="Allow Empty Value", description="Whether or not this field accepts empty value")
	@Override
	public boolean isAllowEmpty() {
		return super.isAllowEmpty();
	}

	@Override
	public void setAllowEmpty(boolean allowEmpty) {
		super.setAllowEmpty(allowEmpty);
	}
	
	@Editable(order=60)
	@io.onedev.server.web.editable.annotation.ShowCondition("isNameOfEmptyValueVisible")
	@NotEmpty
	public String getNameOfEmptyValue() {
		return nameOfEmptyValue;
	}

	public void setNameOfEmptyValue(String nameOfEmptyValue) {
		this.nameOfEmptyValue = nameOfEmptyValue;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNameOfEmptyValueVisible() {
		return (boolean) EditContext.get().getInputValue("allowEmpty");
	}

	@Override
	public void appendCommonAnnotations(StringBuffer buffer, int index) {
		super.appendCommonAnnotations(buffer, index);
		if (getNameOfEmptyValue() != null)
			buffer.append("    @NameOfEmptyValue(\"" + escape(getNameOfEmptyValue()) + "\")");
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null && getIssueSetting().getFieldSpec(showCondition.getInputName()) == null)
			undefinedFields.add(showCondition.getInputName());
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null) {
			FieldSpec field = getIssueSetting().getFieldSpec(showCondition.getInputName());
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(field);
			if (specifiedChoices != null) {
				if (showCondition.getValueMatcher() instanceof ValueIsOneOf) {
					ValueIsOneOf valueIsOneOf = (ValueIsOneOf) showCondition.getValueMatcher(); 
					for (String value: valueIsOneOf.getValues()) {
						if (!specifiedChoices.getChoiceValues().contains(value))
							undefinedFieldValues.add(new UndefinedFieldValue(field.getName(), value));
					}
				} else if (showCondition.getValueMatcher() instanceof ValueIsNotAnyOf) {
					ValueIsNotAnyOf valueIsNotAnyOf = (ValueIsNotAnyOf) showCondition.getValueMatcher(); 
					for (String value: valueIsNotAnyOf.getValues()) {
						if (!specifiedChoices.getChoiceValues().contains(value))
							undefinedFieldValues.add(new UndefinedFieldValue(field.getName(), value));
					}
				}
			}
		}
		return undefinedFieldValues;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null) {
			for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
				if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
					if (entry.getKey().equals(showCondition.getInputName()))
						showCondition.setInputName(entry.getValue().getNewField());
				} else if (showCondition.getInputName().equals(entry.getKey())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		ShowCondition showCondition = getShowCondition();
		if (showCondition != null) {
			for (Map.Entry<String, UndefinedFieldValuesResolution> resolutionEntry: resolutions.entrySet()) {
				if (showCondition.getInputName().equals(resolutionEntry.getKey())) {
					if (showCondition.getValueMatcher() instanceof ValueIsOneOf) {
						ValueIsOneOf valueIsOneOf = (ValueIsOneOf) showCondition.getValueMatcher(); 
						valueIsOneOf.getValues().removeAll(resolutionEntry.getValue().getDeletions());
						for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) {
							int index = valueIsOneOf.getValues().indexOf(renameEntry.getKey());
							if (index != -1) {
								if (valueIsOneOf.getValues().contains(renameEntry.getValue()))
									valueIsOneOf.getValues().remove(index);
								else
									valueIsOneOf.getValues().set(index, renameEntry.getValue());
							}
						}
						if (valueIsOneOf.getValues().isEmpty())
							return false;
					} else if (showCondition.getValueMatcher() instanceof ValueIsNotAnyOf) {
						ValueIsNotAnyOf valueIsNotAnyOf = (ValueIsNotAnyOf) showCondition.getValueMatcher();
						valueIsNotAnyOf.getValues().removeAll(resolutionEntry.getValue().getDeletions());
						for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) {
							int index = valueIsNotAnyOf.getValues().indexOf(renameEntry.getKey());
							if (index != -1) {
								if (valueIsNotAnyOf.getValues().contains(renameEntry.getValue()))
									valueIsNotAnyOf.getValues().remove(index);
								else
									valueIsNotAnyOf.getValues().set(index, renameEntry.getValue());
							}
						}
						if (valueIsNotAnyOf.getValues().isEmpty())
							return false;
					}
				}
			}
		}
		return true;
	}
		
	public void onRenameUser(String oldName, String newName) {
		
	}
	
	public void onRenameGroup(String oldName, String newName) {
		
	}

	public Usage onDeleteUser(String userName) {
		return new Usage();
	}
	
	public Usage onDeleteGroup(String groupName) {
		return new Usage();
	}
	
}
