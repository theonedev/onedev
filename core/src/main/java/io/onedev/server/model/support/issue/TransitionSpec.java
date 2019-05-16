package io.onedev.server.model.support.issue;

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
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.model.support.issue.transitionprerequisite.ValueIsNotAnyOf;
import io.onedev.server.model.support.issue.transitionprerequisite.ValueIsOneOf;
import io.onedev.server.model.support.issue.transitionprerequisite.ValueMatcher;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.support.issue.transitiontrigger.TransitionTrigger;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.util.Input;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.usermatcher.UserMatcher;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldResolution.FixType;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedStateResolution;

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
		for (InputSpec field: getGlobalIssueSetting().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}
	
	public void onRenameUser(String oldName, String newName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			pressButton.setAuthorized(UserMatcher.onRenameUser(pressButton.getAuthorized(), oldName, newName));
		}
	}
	
	public boolean onDeleteUser(String userName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			pressButton.setAuthorized(UserMatcher.onDeleteUser(pressButton.getAuthorized(), userName));
		}
		return false;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			pressButton.setAuthorized(UserMatcher.onRenameGroup(pressButton.getAuthorized(), oldName, newName));
		}
	}
	
	public boolean onDeleteGroup(String groupName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			pressButton.setAuthorized(UserMatcher.onDeleteGroup(pressButton.getAuthorized(), groupName));
		}
		return false;
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
	}
	
	public void onRenameField(String oldName, String newName) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(oldName))
			getPrerequisite().setInputName(newName);
		if (getTrigger() instanceof PressButtonTrigger) {
			PressButtonTrigger trigger = (PressButtonTrigger) getTrigger();
			int index = trigger.getPromptFields().indexOf(oldName);
			if (index != -1) {
				if (trigger.getPromptFields().contains(newName))				
					trigger.getPromptFields().remove(index);
				else
					trigger.getPromptFields().set(index, newName);
			}
		}
		int index = getRemoveFields().indexOf(oldName);
		if (index != -1) {
			if (getRemoveFields().contains(newName))				
				getRemoveFields().remove(index);
			else
				getRemoveFields().set(index, newName);
		}
	}
	
	public boolean onDeleteField(String fieldName) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(fieldName)) 
			setPrerequisite(null);
		if (getTrigger() instanceof PressButtonTrigger) {
			PressButtonTrigger trigger = (PressButtonTrigger) getTrigger();
			for (Iterator<String> it = trigger.getPromptFields().iterator(); it.hasNext();) {
				if (it.next().equals(fieldName))
					it.remove();
			}
		}
		for (Iterator<String> it = getRemoveFields().iterator(); it.hasNext();) {
			if (it.next().equals(fieldName))
				it.remove();
		}
		
		return false;
	}
	
	public boolean onDeleteState(String stateName) {
		fromStates.remove(stateName);
		return fromStates.isEmpty() || toState.equals(stateName);
	}

	public Collection<String> getUndefinedFields(GlobalIssueSetting globalSetting) {
		Collection<String> undefinedFields = new HashSet<>();
		if (getPrerequisite() != null && globalSetting.getFieldSpec(getPrerequisite().getInputName()) == null)
			undefinedFields.add(getPrerequisite().getInputName());
		if (getTrigger() instanceof PressButtonTrigger) {
			PressButtonTrigger trigger = (PressButtonTrigger) getTrigger();
			for (String field: trigger.getPromptFields()) {
				if (globalSetting.getFieldSpec(field) == null)
					undefinedFields.add(field);
			}
		}
		for (String field: getRemoveFields()) {
			if (globalSetting.getFieldSpec(field) == null)
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
		return false;
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
		for (StateSpec state: getGlobalIssueSetting().getStateSpecs())
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
	
	private static GlobalIssueSetting getGlobalIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		if (prerequisite != null) {
			ValueMatcher valueMatcher = prerequisite.getValueMatcher(); 
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getGlobalIssueSetting().getFieldSpec(prerequisite.getInputName()));
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
		return undefinedFieldValues;
	}

	public Collection<? extends String> getUndefinedStates() {
		Collection<String> undefinedStates = new HashSet<>();
		if (getGlobalIssueSetting().getStateSpec(toState) == null)
			undefinedStates.add(toState);
		for (String fromState: fromStates) {
			if (getGlobalIssueSetting().getStateSpec(fromState) == null)
				undefinedStates.add(fromState);
		}
		return undefinedStates;
	}

	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			if (toState.equals(entry.getKey()))
				toState = entry.getValue().getNewState();
			int index = fromStates.indexOf(entry.getKey());
			if (index != -1) {
				if (fromStates.contains(entry.getValue().getNewState()))				
					fromStates.remove(index);
				else
					fromStates.set(index, entry.getValue().getNewState());
			}
		}
	}

}
