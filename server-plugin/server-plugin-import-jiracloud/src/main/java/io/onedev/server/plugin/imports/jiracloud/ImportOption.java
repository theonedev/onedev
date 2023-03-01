package io.onedev.server.plugin.imports.jiracloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class ImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private String assigneeIssueField;
	
	private String dueDateIssueField;
	
	private String timeSpentIssueField;
	
	private String timeEstimateIssueField;
	
	private List<IssueStatusMapping> issueStatusMappings = new ArrayList<>();
	
	private List<IssueTypeMapping> issueTypeMappings = new ArrayList<>();
	
	private List<IssuePriorityMapping> issuePriorityMappings = new ArrayList<>();
	
	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Editable(order=350, description="Specify a user field to hold assignee information.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	@ChoiceProvider("getAssigneesIssueFieldChoices")
	@NotEmpty
	public String getAssigneeIssueField() {
		return assigneeIssueField;
	}

	public void setAssigneeIssueField(String assigneeIssueField) {
		this.assigneeIssueField = assigneeIssueField;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAssigneesIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.USER) && field.isAllowMultiple())
				choices.add(field.getName());
		}
		return choices;
	}

	@Editable(order=360, description="Optionally specify a date field to hold due date information.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	@ChoiceProvider("getDueDateIssueFieldChoices")
	public String getDueDateIssueField() {
		return dueDateIssueField;
	}

	public void setDueDateIssueField(String dueDateIssueField) {
		this.dueDateIssueField = dueDateIssueField;
	}

	@SuppressWarnings("unused")
	private static List<String> getDueDateIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.DATE))
				choices.add(field.getName());
		}
		return choices;
	}
	
	@Editable(order=370, description="Optionally specify a working period field to hold time spent infomration.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	@ChoiceProvider("getWorkingPeriodIssueFieldChoices")
	public String getTimeSpentIssueField() {
		return timeSpentIssueField;
	}

	public void setTimeSpentIssueField(String timeSpentIssueField) {
		this.timeSpentIssueField = timeSpentIssueField;
	}

	@Editable(order=380, description="Optionally specify a working period field to hold time estimate infomration.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	@ChoiceProvider("getWorkingPeriodIssueFieldChoices")
	public String getTimeEstimateIssueField() {
		return timeEstimateIssueField;
	}

	public void setTimeEstimateIssueField(String timeEstimateIssueField) {
		this.timeEstimateIssueField = timeEstimateIssueField;
	}

	@SuppressWarnings("unused")
	private static List<String> getWorkingPeriodIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.WORKING_PERIOD))
				choices.add(field.getName());
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getVersionIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.TEXT))
				choices.add(field.getName());
		}
		return choices;
	}
	
	@Editable(order=600, description="Specify how to map JIRA issue statuses to OneDev custom fields.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here")
	public List<IssueStatusMapping> getIssueStatusMappings() {
		return issueStatusMappings;
	}

	public void setIssueStatusMappings(List<IssueStatusMapping> issueStatusMappings) {
		this.issueStatusMappings = issueStatusMappings;
	}

	@Editable(order=700, description="Specify how to map JIRA issue types to OneDev custom fields.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	public List<IssueTypeMapping> getIssueTypeMappings() {
		return issueTypeMappings;
	}

	public void setIssueTypeMappings(List<IssueTypeMapping> issueTypeMappings) {
		this.issueTypeMappings = issueTypeMappings;
	}

	@Editable(order=800, description="Specify how to map JIRA issue priorities to OneDev custom fields.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	public List<IssuePriorityMapping> getIssuePriorityMappings() {
		return issuePriorityMappings;
	}

	public void setIssuePriorityMappings(List<IssuePriorityMapping> issuePriorityMappings) {
		this.issuePriorityMappings = issuePriorityMappings;
	}

}
