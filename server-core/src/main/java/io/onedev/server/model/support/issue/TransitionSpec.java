package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.support.issue.transitiontrigger.TransitionTrigger;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution.FixType;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class TransitionSpec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> fromStates;
	
	private String toState;
	
	private TransitionTrigger trigger;
	
	private List<String> removeFields = new ArrayList<>();
	
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
		return getTrigger().onDeleteBranch(branchName).prefix("trigger");
	}
	
	public void onRenameRole(String oldName, String newName) {
		getTrigger().onRenameRole(oldName, newName);
	}
	
	public Usage onDeleteRole(String roleName) {
		return trigger.onDeleteRole(roleName).prefix("trigger");
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
		return StringUtils.join(getFromStates()) + "->" + getToState();		
	}

	public boolean canTransitManually(Issue issue, @Nullable String toState) {
		if (getFromStates().contains(issue.getState()) 
				&& (toState == null || toState.equals(getToState())) 
				&& getTrigger() instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) getTrigger();
			if (pressButton.isAuthorized(issue.getProject())) {
				IssueQuery parsedQuery = IssueQuery.parse(issue.getProject(), 
						getTrigger().getIssueQuery(), true, true, true, true, true);
				return parsedQuery.matches(issue);
			}
		}
		return false;
	}
	
	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
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

	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(getTrigger().getUndefinedFields());
		GlobalIssueSetting setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (String field: getRemoveFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return trigger.getUndefinedFieldValues();
	}

	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedStateResolution.FixType.CHANGE_TO_ANOTHER_STATE) {
				ReconcileUtils.renameItem(fromStates, entry.getKey(), entry.getValue().getNewState());
				if (toState.equals(entry.getKey()))
					toState = entry.getValue().getNewState();
			} else {
				fromStates.remove(entry.getKey());
				if (fromStates.isEmpty() || toState.equals(entry.getKey())) 
					return false;
			}
		}
		return trigger.fixUndefinedStates(resolutions);
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == FixType.CHANGE_TO_ANOTHER_FIELD) 
				ReconcileUtils.renameItem(getRemoveFields(), entry.getKey(), entry.getValue().getNewField());
			else 
				getRemoveFields().remove(entry.getKey());
		}
		return trigger.fixUndefinedFields(resolutions);
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		return trigger.fixUndefinedFieldValues(resolutions);
	}
	
}
