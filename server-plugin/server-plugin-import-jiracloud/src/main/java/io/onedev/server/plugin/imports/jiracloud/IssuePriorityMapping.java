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
public class IssuePriorityMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jiraIssuePriority;
	
	private String oneDevIssueField;

	@Editable(order=100, name="JIRA Issue Priority")
	@NotEmpty
	public String getJiraIssuePriority() {
		return jiraIssuePriority;
	}

	public void setJiraIssuePriority(String jiraIssuePriority) {
		this.jiraIssuePriority = jiraIssuePriority;
	}

	@Editable(order=200, name="OneDev Issue Field", description="Specify a custom field of Enum type")
	@ChoiceProvider("getOneDevIssueFieldChoices")
	@NotEmpty
	public String getOneDevIssueField() {
		return oneDevIssueField;
	}

	public void setOneDevIssueField(String oneDevIssueField) {
		this.oneDevIssueField = oneDevIssueField;
	}

	@SuppressWarnings("unused")
	private static List<String> getOneDevIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs()) {
			if (field.getType().equals(InputSpec.ENUMERATION)) {
				for (String value: field.getPossibleValues()) 
					choices.add(field.getName() + "::" + value);
			}
		}
		return choices;
	}
	
}
