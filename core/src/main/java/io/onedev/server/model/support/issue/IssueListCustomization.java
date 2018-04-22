package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

public class IssueListCustomization implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<IssueQuery> presetQueries = new ArrayList<>();
	
	private List<String> displayFields = new ArrayList<>();
	
	public List<IssueQuery> getPresetQueries() {
		return presetQueries;
	}

	public void setPresetQueries(List<IssueQuery> presetQueries) {
		this.presetQueries = presetQueries;
	}

	@ChoiceProvider("getFieldChoices")
	public List<String> getDisplayFields() {
		return displayFields;
	}

	public void setDisplayFields(List<String> displayFields) {
		this.displayFields = displayFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFields())
			fields.add(field.getName());
		return fields;
	}

	public void onRenameField(String oldName, String newName) {
		int index = getDisplayFields().indexOf(oldName);
		if (index != -1)
			getDisplayFields().set(index, newName);
	}
	
	public void onDeleteField(String fieldName) {
		getDisplayFields().remove(fieldName);
	}

	public IssueQuery getQuery() {
		if (!presetQueries.isEmpty()) {
			return presetQueries.iterator().next();
		} else {
			return new IssueQuery(null, new ArrayList<>());
		}
	}
	
}
