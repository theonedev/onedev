package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.util.IssueConstants;

public abstract class FieldCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		if (!IssueConstants.QUERY_FIELDS.contains(fieldName) 
				&& issueSetting.getFieldSpec(fieldName) == null) {
			undefinedFields.add(fieldName);
		}
		return undefinedFields;
	}

	@Override
	public void onRenameField(String oldField, String newField) {
		if (oldField.equals(fieldName))
			fieldName = newField;
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		return fieldName.equals(this.fieldName);
	}
	
}
