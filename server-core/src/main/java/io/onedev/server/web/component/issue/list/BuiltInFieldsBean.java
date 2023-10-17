package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.MilestoneChoice;

@Editable
public class BuiltInFieldsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME_STATE = "State";
	
	public static final String NAME_CONFIDENTIAL = "Confidential";
	
	public static final String NAME_MILESTONES = "Milestones";
	
	public static final String PROP_STATE = "state";
	
	public static final String PROP_CONFIDENTIAL = "confidential";
	
	public static final String PROP_MILESTONES = "milestones";
	
	private String state;
	
	private boolean confidential;
	
	private List<String> milestones;

	@Editable(order=100)
	@ChoiceProvider("getStateChoices")
	@NotEmpty
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Editable(order=150)
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	@Editable(order=200)
	@MilestoneChoice
	public List<String> getMilestones() {
		return milestones;
	}

	public void setMilestones(List<String> milestones) {
		this.milestones = milestones;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return issueSetting.getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
