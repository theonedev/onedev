package io.onedev.server.plugin.imports.jiracloud;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class IssueStatusMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jiraIssueStatus;
	
	private String oneDevIssueState;

	@Editable(order=100, name="JIRA Issue Status")
	@NotEmpty
	public String getJiraIssueStatus() {
		return jiraIssueStatus;
	}

	public void setJiraIssueStatus(String jiraIssueStatus) {
		this.jiraIssueStatus = jiraIssueStatus;
	}

	@Editable(order=200, name="OneDev Issue State", description="OneDev Issue State")
	@ChoiceProvider("getOneDevIssueStateChoices")
	@NotEmpty
	public String getOneDevIssueState() {
		return oneDevIssueState;
	}

	public void setOneDevIssueState(String oneDevIssueState) {
		this.oneDevIssueState = oneDevIssueState;
	}

	@SuppressWarnings("unused")
	private static List<String> getOneDevIssueStateChoices() {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return issueSetting.getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
