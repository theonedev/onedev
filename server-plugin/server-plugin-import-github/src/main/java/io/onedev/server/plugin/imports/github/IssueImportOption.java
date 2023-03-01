package io.onedev.server.plugin.imports.github;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class IssueImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private String closedIssueState;
	
	private String assigneesIssueField;
	
	private List<IssueLabelMapping> issueLabelMappings = new ArrayList<>();
	
	@Editable(order=300, description="Specify which issue state to use for closed GitHub issues.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here")
	@ChoiceProvider("getCloseStateChoices")
	@NotEmpty
	public String getClosedIssueState() {
		return closedIssueState;
	}

	public void setClosedIssueState(String closedIssueState) {
		this.closedIssueState = closedIssueState;
	}

	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@SuppressWarnings("unused")
	private static List<String> getCloseStateChoices() {
		List<String> choices = getIssueSetting().getStateSpecs().stream()
				.map(it->it.getName()).collect(Collectors.toList());
		choices.remove(0);
		return choices;
	}
	
	@Editable(order=350, description="Specify a multi-value user field to hold assignees information."
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	@ChoiceProvider("getAssigneesIssueFieldChoices")
	@NotEmpty
	public String getAssigneesIssueField() {
		return assigneesIssueField;
	}

	public void setAssigneesIssueField(String assigneesIssueField) {
		this.assigneesIssueField = assigneesIssueField;
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

	@Editable(order=400, description="Specify how to map GitHub issue labels to OneDev custom "
			+ "fields.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	public List<IssueLabelMapping> getIssueLabelMappings() {
		return issueLabelMappings;
	}

	public void setIssueLabelMappings(List<IssueLabelMapping> issueLabelMappings) {
		this.issueLabelMappings = issueLabelMappings;
	}

}
