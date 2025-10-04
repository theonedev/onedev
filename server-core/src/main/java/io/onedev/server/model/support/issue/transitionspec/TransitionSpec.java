package io.onedev.server.model.support.issue.transitionspec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution.FixType;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Editable
public abstract class TransitionSpec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> fromStates = new ArrayList<>();
	
	private String issueQuery;
	
	private List<String> removeFields = new ArrayList<>();
	
	@Editable(order=10, placeholder = "Any state")
	@ChoiceProvider("getStateChoices")
	public List<String> getFromStates() {
		return fromStates;
	}

	public void setFromStates(List<String> fromStates) {
		this.fromStates = fromStates;
	}

	@Editable(order=9900, name="Applicable Issues", placeholder="All", description=""
			+ "Optionally specify issues applicable for this transition. Leave empty for all issues. ")
	@io.onedev.server.annotation.IssueQuery(withOrder = false)
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(String issueQuery) {
		this.issueQuery = issueQuery;
	}
	
	@Editable(order=10000, placeholder="No fields to remove", description=""
			+ "Optionally select fields to remove when this transition happens")
	@ChoiceProvider("getFieldChoices")
	public List<String> getRemoveFields() {
		return removeFields;
	}

	public void setRemoveFields(List<String> removeFields) {
		this.removeFields = removeFields;
	}
	
	protected static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	public Usage onDeleteBranch(String branchName) {
		return new Usage();
	}
	
	public void onRenameRole(String oldName, String newName) {
	}
	
	public Usage onDeleteRole(String roleName) {
		return new Usage();
	}
	
	public void onRenameLink(String oldName, String newName) {
		getQueryUpdater().onRenameLink(oldName, newName);
	}
	
	public Usage onDeleteLink(String linkName) {
		return getQueryUpdater().onDeleteLink(linkName);
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		getQueryUpdater().onMoveProject(oldPath, newPath);
	}
	
	public Usage onDeleteProject(String projectPath) {
		return getQueryUpdater().onDeleteProject(projectPath);
	}
	
	protected static List<String> getStateChoices() {
		List<String> stateNames = new ArrayList<>();
		for (StateSpec state: getIssueSetting().getStateSpecs())
			stateNames.add(state.getName());
		return stateNames;
	}
	
	protected static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingService.class).getIssueSetting();
	}

	public Collection<String> getUndefinedStates() {
		Collection<String> undefinedStates = new HashSet<>();
		for (String fromState: getFromStates()) {
			if (getIssueSetting().getStateSpec(fromState) == null)
				undefinedStates.add(fromState);
		}
		undefinedStates.addAll(getQueryUpdater().getUndefinedStates());
		return undefinedStates;
	}

	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(getQueryUpdater().getUndefinedFields());
		GlobalIssueSetting setting = OneDev.getInstance(SettingService.class).getIssueSetting();
		for (String field: getRemoveFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return getQueryUpdater().getUndefinedFieldValues();
	}

	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedStateResolution.FixType.CHANGE_TO_ANOTHER_STATE) {
				ReconcileUtils.renameItem(getFromStates(), entry.getKey(), entry.getValue().getNewState());
			} else {
				getFromStates().remove(entry.getKey());
				if (getFromStates().isEmpty()) 
					return false;
			}
		}
		return getQueryUpdater().fixUndefinedStates(resolutions);
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == FixType.CHANGE_TO_ANOTHER_FIELD) 
				ReconcileUtils.renameItem(getRemoveFields(), entry.getKey(), entry.getValue().getNewField());
			else 
				getRemoveFields().remove(entry.getKey());
		}
		return getQueryUpdater().fixUndefinedFields(resolutions);
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		return getQueryUpdater().fixUndefinedFieldValues(resolutions);
	}
	
	public IssueQueryUpdater getQueryUpdater() {
		return new IssueQueryUpdater() {

			@Override
			protected Usage getUsage() {
				return new Usage().add("applicable issues");
			}

			@Override
			protected boolean isAllowEmpty() {
				return true;
			}

			@Override
			protected String getIssueQuery() {
				return issueQuery;
			}

			@Override
			protected void setIssueQuery(String issueQuery) {
				TransitionSpec.this.issueQuery = issueQuery;
			}

		};
	}

	@Override
	public String toString() {
		return "[" + (getFromStates().isEmpty()?"Any state":StringUtils.join(getFromStates(), ",")) + "] -> [" + (getToStates().isEmpty()?"Any state":StringUtils.join(getToStates(), ",")) + "]";
	}

	public abstract List<String> getToStates();

	public abstract String getTriggerDescription();
	
}
