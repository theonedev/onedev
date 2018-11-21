package io.onedev.server.web.page.project.issues.workflowreconcile;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class UndefinedStateResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	private String newState;

	@Editable
	@ChoiceProvider("getNewStateChoices")
	@OmitName
	@NotEmpty
	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getNewStateChoices() {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return issueSetting.getStateSpecs().stream().map(each->each.getName()).collect(Collectors.toList());
	}
	
}
