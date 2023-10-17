package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class IssueStateMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String youTrackIssueState;
	
	private String oneDevIssueState;

	@Editable(order=100, name="YouTrack Issue State")
	@NotEmpty
	public String getYouTrackIssueState() {
		return youTrackIssueState;
	}

	public void setYouTrackIssueState(String youTrackIssueState) {
		this.youTrackIssueState = youTrackIssueState;
	}

	@Editable(order=200, name="OneDev Issue State")
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
		List<String> choices = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (StateSpec state: issueSetting.getStateSpecs()) 
			choices.add(state.getName());
		return choices;
	}
	
}
