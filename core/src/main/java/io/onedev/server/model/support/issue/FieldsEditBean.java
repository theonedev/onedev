package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class FieldsEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> fields = new ArrayList<>();

	@Editable
	@ChoiceProvider("getFieldChoices")
	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

}
