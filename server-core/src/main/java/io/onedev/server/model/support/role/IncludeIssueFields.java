package io.onedev.server.model.support.role;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=300, name="Specified fields")
public class IncludeIssueFields implements IssueFieldSet {

	private static final long serialVersionUID = 1L;

	private List<String> includeFields = new ArrayList<>();

	@Editable(name="Fields")
	@ChoiceProvider("getFieldChoices")
	@OmitName
	@Size(min=1, message = "At least one field needs to be specified")
	public List<String> getIncludeFields() {
		return includeFields;
	}

	public void setIncludeFields(List<String> includeFields) {
		this.includeFields = includeFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}

}
