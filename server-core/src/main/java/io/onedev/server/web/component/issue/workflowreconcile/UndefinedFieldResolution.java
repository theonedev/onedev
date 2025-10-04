package io.onedev.server.web.component.issue.workflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;

@Editable
public class UndefinedFieldResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_FIELD, DELETE_THIS_FIELD}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_FIELD;
	
	private String newField;
	
	@Editable(order=50)
	@NotNull
	@OmitName
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100, name="new field")
	@ChoiceProvider("getFieldChoices")
	@DependsOn(property="fixType", value="CHANGE_TO_ANOTHER_FIELD")
	@NotEmpty
	@OmitName
	public String getNewField() {
		return newField;
	}

	public void setNewField(String newField) {
		this.newField = newField;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		for (FieldSpec field: OneDev.getInstance(SettingService.class).getIssueSetting().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}
	
}
