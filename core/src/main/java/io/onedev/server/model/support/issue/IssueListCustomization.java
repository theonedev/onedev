package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueSort;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

public class IssueListCustomization implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<IssueQuery> presetQueries = new ArrayList<>();
	
	private List<String> displayFields = new ArrayList<>();
	
	private List<IssueSort> sorts = new ArrayList<>();

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

	@NotNull
	public List<IssueSort> getSorts() {
		return sorts;
	}

	public void setSorts(List<IssueSort> sorts) {
		this.sorts = sorts;
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
		for (IssueSort order: getSorts()) {
			if (order.getField().equals(oldName))
				order.setField(newName);
		}
	}
	
	public void onDeleteField(String fieldName) {
		getDisplayFields().remove(fieldName);
		for (Iterator<IssueSort> it = getSorts().iterator(); it.hasNext();) {
			if (it.next().equals(fieldName))
				it.remove();
		}
	}

	public IssueQuery getQuery() {
		if (!presetQueries.isEmpty()) {
			return presetQueries.iterator().next();
		} else {
			return new IssueQuery();
		}
	}
	
}
