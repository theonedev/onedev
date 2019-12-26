package io.onedev.server.model.support.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=200, name="All except")
public class ExcludeIssueFields implements IssueFieldSet {

	private static final long serialVersionUID = 1L;

	private List<String> excludeFields = new ArrayList<>();

	@Editable(name="Excluded Fields")
	@ChoiceProvider("getFieldChoices")
	@OmitName
	@Size(min=1, message = "At least one field needs to be specified")
	public List<String> getExcludeFields() {
		return excludeFields;
	}

	public void setExcludeFields(List<String> excludeFields) {
		this.excludeFields = excludeFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}

	@Override
	public Collection<String> getIncludeFields() {
		Collection<String> fields = OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
		fields.removeAll(excludeFields);
		return fields;
	}

}
