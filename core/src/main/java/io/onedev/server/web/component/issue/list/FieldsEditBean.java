package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class FieldsEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> fields = new ArrayList<>();

	@Editable(description="<b>Hint:</b> Selected fields can be dragged and reordered")
	@ChoiceProvider("getFieldChoices")
	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>(IssueConstants.DISPLAY_FIELDS);
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

}
