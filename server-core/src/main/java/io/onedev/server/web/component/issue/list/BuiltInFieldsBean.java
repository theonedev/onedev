package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.MilestoneChoice;

@Editable
public class BuiltInFieldsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String state;
	
	private String milestone;

	@Editable(order=100)
	@ChoiceProvider("getStateChoices")
	@NotEmpty
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Editable(order=200)
	@MilestoneChoice
	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return issueSetting.getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
