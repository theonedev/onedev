package io.onedev.server.web.component.issue.workflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable
public class UndefinedStateResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_STATE, DELETE_THIS_STATE}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_STATE;
	
	private String newState;

	@Editable(order=50)
	@NotNull
	@OmitName
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100)
	@ChoiceProvider("getStateChoices")
	@ShowCondition("isNewStateVisible")
	@OmitName
	@NotEmpty
	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNewStateVisible() {
		return EditContext.get().getInputValue("fixType") == FixType.CHANGE_TO_ANOTHER_STATE;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return new ArrayList<>(issueSetting.getStateSpecMap().keySet());
	}
	
}
