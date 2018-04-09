package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.ShowCondition;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class FieldFixOption implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_FIELD, DELETE_THIS_FIELD}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_FIELD;
	
	private String newField;
	
	private boolean fixAll;

	@Editable(order=50)
	@NotNull
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100, name="Change to Field", description="Select existing field to change the invalid field to. "
			+ "Only those with same field type will be listed here")
	@ChoiceProvider("getFieldChoices")
	@ShowCondition("isNewFieldVisible")
	@NotEmpty
	public String getNewField() {
		return newField;
	}

	public void setNewField(String newField) {
		this.newField = newField;
	}

	@Editable(order=200, description="Enable to fix the problem for all issues in the project, "
			+ "otherwise only fix for current issue")
	public boolean isFixAll() {
		return fixAll;
	}

	public void setFixAll(boolean fixAll) {
		this.fixAll = fixAll;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNewFieldVisible() {
		return OneContext.get().getEditContext().getOnScreenValue("fixType") == FixType.CHANGE_TO_ANOTHER_FIELD;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		MultiValueIssueField fixField = OneContext.get().getComponent().findParent(FieldFixPanel.class).getInvalidField();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		List<String> fields = new ArrayList<>();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFields()) {
			if (EditableUtils.getDisplayName(field.getClass()).equals(fixField.getType()))
				fields.add(field.getName());
		}
		return fields;
	}
	
}
