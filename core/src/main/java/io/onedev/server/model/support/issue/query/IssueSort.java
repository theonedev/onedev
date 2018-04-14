package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable
public class IssueSort implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public enum Direction {ASCENDING, DESCENDING};
	
	private static List<String> BUILTIN_FIELDS = Lists.newArrayList("State", "Report Date", "Votes");
	
	private String field;
	
	private IssueSort.Direction direction = Direction.ASCENDING;

	@Editable(order=100)
	@ChoiceProvider("getFieldChoices")
	@NotEmpty
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	@Editable(order=200)
	@NotNull
	public IssueSort.Direction getDirection() {
		return direction;
	}

	public void setDirection(IssueSort.Direction direction) {
		this.direction = direction;
	}

	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>(IssueSort.BUILTIN_FIELDS);
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFields()) {
			if (field instanceof ChoiceInput)
				fields.add(field.getName());
		}
		return fields;
	}
	
}