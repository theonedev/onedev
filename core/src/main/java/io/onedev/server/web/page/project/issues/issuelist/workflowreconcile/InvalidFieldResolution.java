package io.onedev.server.web.page.project.issues.issuelist.workflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.util.editable.annotation.ShowCondition;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.WorkflowReconcilePanel.InvalidFieldContainer;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class InvalidFieldResolution implements Serializable {

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

	@Editable(order=100, name="Change to Field")
	@ChoiceProvider("getFieldChoices")
	@ShowCondition("isNewFieldVisible")
	@NotEmpty
	@OmitName
	public String getNewField() {
		return newField;
	}

	public void setNewField(String newField) {
		this.newField = newField;
	}

	@SuppressWarnings("unused")
	private static boolean isNewFieldVisible() {
		return OneContext.get().getEditContext().getInputValue("fixType") == FixType.CHANGE_TO_ANOTHER_FIELD;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		InvalidFieldContainer container = ((ComponentContext)OneContext.get()).getComponent().findParent(InvalidFieldContainer.class);
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		List<String> fields = new ArrayList<>();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFieldSpecs()) {
			if (EditableUtils.getDisplayName(field.getClass()).equals(container.getFieldType()))
				fields.add(field.getName());
		}
		return fields;
	}
	
}
