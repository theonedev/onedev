package io.onedev.server.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.issue.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.issue.transitionprerequisite.ValueIsNotAnyOf;
import io.onedev.server.issue.transitionprerequisite.ValueIsOneOf;
import io.onedev.server.issue.transitionprerequisite.ValueMatcher;
import io.onedev.server.issue.transitiontrigger.TransitionTrigger;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.util.Input;
import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution.FixType;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class TransitionSpec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String description;
	
	private List<String> fromStates;
	
	private String toState;
	
	private TransitionPrerequisite prerequisite;
	
	private TransitionTrigger trigger;
	
	private List<String> removeFields = new ArrayList<>();
	
	@Editable(order=50)
	@NameOfEmptyValue("No description")
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Editable(order=100)
	@Size(min=1, message="At least one state needs to be specified")
	@ChoiceProvider("getStateChoices")
	public List<String> getFromStates() {
		return fromStates;
	}

	public void setFromStates(List<String> fromStates) {
		this.fromStates = fromStates;
	}

	@Editable(order=200)
	@NotEmpty
	@ChoiceProvider("getStateChoices")
	public String getToState() {
		return toState;
	}

	public void setToState(String toState) {
		this.toState = toState;
	}

	@Editable(order=300, description="Enable if applicability of this transition depends on "
			+ "value of particular field")
	@NameOfEmptyValue("No prerequisite")
	public TransitionPrerequisite getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(TransitionPrerequisite prerequisite) {
		this.prerequisite = prerequisite;
	}

	@Editable(order=400, name="Do Transition When")
	@NotNull(message="may not be empty")
	public TransitionTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(TransitionTrigger trigger) {
		this.trigger = trigger;
	}
	
	@Editable(order=1000, description="Optionally select fields to remove when this transition happens")
	@ChoiceProvider("getFieldChoices")
	@NameOfEmptyValue("No fields to remove")
	public List<String> getRemoveFields() {
		return removeFields;
	}

	public void setRemoveFields(List<String> removeFields) {
		this.removeFields = removeFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}
	
	public Usage onDeleteBranch(String branchName) {
		return getTrigger().onDeleteBranch(branchName).prefix("transitions: " + fromStates + "->" + toState);
	}
	
	public void onRenameRole(String oldName, String newName) {
		getTrigger().onRenameRole(oldName, newName);
	}
	
	public Usage onDeleteRole(String roleName) {
		return trigger.onDeleteRole(roleName).prefix("transitions: " + fromStates + "->" + toState);
	}
	
	public void onRenameState(String oldName, String newName) {
		int index = fromStates.indexOf(oldName);
		if (index != -1) {
			if (fromStates.contains(newName))
				fromStates.remove(index);
			else
				fromStates.set(index, newName);
		}
		if (toState.equals(oldName))
			toState = newName;
		trigger.onRenameState(oldName, newName);
	}
	
	public void onRenameField(String oldName, String newName) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(oldName))
			getPrerequisite().setInputName(newName);
		int index = getRemoveFields().indexOf(oldName);
		if (index != -1) {
			if (getRemoveFields().contains(newName))				
				getRemoveFields().remove(index);
			else
				getRemoveFields().set(index, newName);
		}
		trigger.onRenameField(oldName, newName);
	}
	
	public boolean onDeleteField(String fieldName) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(fieldName)) 
			setPrerequisite(null);
		
		if (getTrigger().onDeleteField(fieldName))
			return true;
		
		for (Iterator<String> it = getRemoveFields().iterator(); it.hasNext();) {
			if (it.next().equals(fieldName))
				it.remove();
		}
		
		return false;
	}
	
	public boolean onDeleteState(String stateName) {
		fromStates.remove(stateName);
		if (fromStates.isEmpty() || toState.equals(stateName)) 
			return true;
		else
			return trigger.onDeleteState(stateName);
	}

	public Collection<String> getUndefinedFields(Project project) {
		Collection<String> undefinedFields = new HashSet<>();
		GlobalIssueSetting setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		if (getPrerequisite() != null && setting.getFieldSpec(getPrerequisite().getInputName()) == null)
			undefinedFields.add(getPrerequisite().getInputName());
		undefinedFields.addAll(getTrigger().getUndefinedFields());
		for (String field: getRemoveFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		return undefinedFields;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == FixType.CHANGE_TO_ANOTHER_FIELD)
				onRenameField(entry.getKey(), entry.getValue().getNewField());
			else if (onDeleteField(entry.getKey()))
				return true;
		}
		return false;
	}
	
	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(fieldName)) {
			if (getPrerequisite().getValueMatcher() instanceof ValueIsOneOf) {
				ValueIsOneOf valueIsOneOf = (ValueIsOneOf) getPrerequisite().getValueMatcher(); 
				valueIsOneOf.getValues().removeAll(valueSetEdit.getDeletions());
				for (Map.Entry<String, String> entry: valueSetEdit.getRenames().entrySet()) {
					int index = valueIsOneOf.getValues().indexOf(entry.getKey());
					if (index != -1) {
						if (valueIsOneOf.getValues().contains(entry.getValue()))
							valueIsOneOf.getValues().remove(index);
						else
							valueIsOneOf.getValues().set(index, entry.getValue());
					}
				}
				if (valueIsOneOf.getValues().isEmpty())
					setPrerequisite(null);
			} else if (getPrerequisite().getValueMatcher() instanceof ValueIsNotAnyOf) {
				ValueIsNotAnyOf valueIsNotAnyOf = (ValueIsNotAnyOf) getPrerequisite().getValueMatcher();
				valueIsNotAnyOf.getValues().removeAll(valueSetEdit.getDeletions());
				for (Map.Entry<String, String> entry: valueSetEdit.getRenames().entrySet()) {
					int index = valueIsNotAnyOf.getValues().indexOf(entry.getKey());
					if (index != -1) {
						if (valueIsNotAnyOf.getValues().contains(entry.getValue()))
							valueIsNotAnyOf.getValues().remove(index);
						else
							valueIsNotAnyOf.getValues().set(index, entry.getValue());
					}
				}
				if (valueIsNotAnyOf.getValues().isEmpty())
					setPrerequisite(null);
			}
		}
		return trigger.onEditFieldValues(fieldName, valueSetEdit);
	}
	
	public boolean fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits) {
		for (Map.Entry<String, ValueSetEdit> entry: valueSetEdits.entrySet()) {
			if (onEditFieldValues(entry.getKey(), entry.getValue()))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		List<String> stateNames = new ArrayList<>();
		for (StateSpec state: getIssueSetting().getStateSpecs())
			stateNames.add(state.getName());
		return stateNames;
	}
	
	@Override
	public String toString() {
		return StringUtils.join(getFromStates()) + "-->" + getToState();		
	}

	public boolean canTransite(Issue issue) {
		if (getFromStates().contains(issue.getState())) {
			if (getPrerequisite() == null) {
				return true;
			} else {
				Input field = issue.getFieldInputs().get(getPrerequisite().getInputName());
				List<String> fieldValues;
				if (field != null)
					fieldValues = field.getValues();
				else
					fieldValues = new ArrayList<>();
				if (getPrerequisite().matches(fieldValues))
					return true;
			}
		}
		return false;
	}
	
	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		if (prerequisite != null) {
			ValueMatcher valueMatcher = prerequisite.getValueMatcher(); 
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(prerequisite.getInputName()));
			if (specifiedChoices != null) {
				if (valueMatcher instanceof ValueIsOneOf) {
					ValueIsOneOf valueIsOneOf = (ValueIsOneOf) valueMatcher;
					for (String value: valueIsOneOf.getValues()) {
						if (!specifiedChoices.getChoiceValues().contains(value))
							undefinedFieldValues.add(new UndefinedFieldValue(prerequisite.getInputName(), value));
					}
				} else if (valueMatcher instanceof ValueIsNotAnyOf) {
					ValueIsNotAnyOf valueIsNotAnyOf = (ValueIsNotAnyOf) valueMatcher;
					for (String value: valueIsNotAnyOf.getValues()) {
						if (!specifiedChoices.getChoiceValues().contains(value))
							undefinedFieldValues.add(new UndefinedFieldValue(prerequisite.getInputName(), value));
					}
				}
			}
		}
		undefinedFieldValues.addAll(trigger.getUndefinedFieldValues());
		return undefinedFieldValues;
	}

	public Collection<? extends String> getUndefinedStates() {
		Collection<String> undefinedStates = new HashSet<>();
		if (getIssueSetting().getStateSpec(toState) == null)
			undefinedStates.add(toState);
		for (String fromState: fromStates) {
			if (getIssueSetting().getStateSpec(fromState) == null)
				undefinedStates.add(fromState);
		}
		undefinedStates.addAll(trigger.getUndefinedStates());
		return undefinedStates;
	}

	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet())
			onRenameState(entry.getKey(), entry.getValue().getNewState());
	}

}
