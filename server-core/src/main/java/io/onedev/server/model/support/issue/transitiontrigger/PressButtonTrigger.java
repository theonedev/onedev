package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.usermatcher.UserMatcher;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=100, name="Button is pressed")
public class PressButtonTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String buttonLabel;

	private String authorized;
	
	private List<String> promptFields = new ArrayList<>();
	
	@Editable(order=100)
	@NotEmpty
	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Editable(order=200, name="Authorization")
	@io.onedev.server.web.editable.annotation.UserMatcher
	@NotEmpty(message="may not be empty")
	public String getAuthorized() {
		return authorized;
	}
	
	public void setAuthorized(String authorized) {
		this.authorized = authorized;
	}

	@Editable(order=500, description="Optionally select fields to prompt when this button is pressed")
	@ChoiceProvider("getFieldChoices")
	@NameOfEmptyValue("No fields to prompt")
	public List<String> getPromptFields() {
		return promptFields;
	}

	public void setPromptFields(List<String> promptFields) {
		this.promptFields = promptFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	public void onRenameField(String oldName, String newName) {
		for (int i=0; i<getPromptFields().size(); i++) {
			if (getPromptFields().get(i).equals(oldName))
				getPromptFields().set(i, newName);
		}
	}
	
	public void onDeleteField(String fieldName) {
		for (Iterator<String> it = getPromptFields().iterator(); it.hasNext();) {
			if (it.next().equals(fieldName))
				it.remove();
		}
	}	
	
	public boolean isAuthorized() {
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		User user = SecurityUtils.getUser();
		return user != null && UserMatcher.fromString(getAuthorized()).matches(page.getProject(), user);		
	}
	
}
