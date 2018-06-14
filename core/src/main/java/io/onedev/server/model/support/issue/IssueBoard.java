package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;

@Editable
public class IssueBoard implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String issueFilter;
	
	private String identifyField;
	
	private List<String> columns = new ArrayList<>();

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Issue Filter", description="Optionally specify a query to filter issues of the board")
	@IssueQuery(allowSort=false)
	@Nullable
	public String getIssueFilter() {
		return issueFilter;
	}

	public void setIssueFilter(String issueFilter) {
		this.issueFilter = issueFilter;
	}

	@Editable(order=300, description="Specify issue field to identify different columns of the board")
	@ChoiceProvider("getIdentifyFieldChoices")
	@NotEmpty
	public String getIdentifyField() {
		return identifyField;
	}

	public void setIdentifyField(String identifyField) {
		this.identifyField = identifyField;
	}

	@Editable(order=400, description="Specify columns of the board. Each column corresponds to "
			+ "a value of the issue field specified above")
	@Size(min=2, message="At least two columns need to be defined")
	@ChoiceProvider("getColumnChoices")
	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	@SuppressWarnings("unused")
	private static List<String> getIdentifyFieldChoices() {
		List<String> choices = new ArrayList<>();
		Project project = OneContext.get().getProject();
		choices.add(Issue.STATE);
		for (InputSpec fieldSpec: project.getIssueWorkflow().getFieldSpecs()) {
			if (!fieldSpec.isAllowMultiple() && (fieldSpec instanceof ChoiceInput || fieldSpec instanceof UserChoiceInput || fieldSpec instanceof GroupChoiceInput)) {
				choices.add(fieldSpec.getName());
			}
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> getColumnChoices() {
		Map<String, String> choices = new LinkedHashMap<>();
		Project project = OneContext.get().getProject();
		String fieldName = (String) OneContext.get().getEditContext().getInputValue("identifyField");
		if (Issue.STATE.equals(fieldName)) {
			for (StateSpec state: project.getIssueWorkflow().getStateSpecs())
				choices.put(state.getName(), state.getName());
		} else if (fieldName != null) {
			InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
			for (String each: fieldSpec.getPossibleValues())
				choices.put(each, each);
			if (fieldSpec.isAllowEmpty())
				choices.put(fieldSpec.getNameOfEmptyValue(), null);
		} 
		return choices;
	}
	
	public static int getBoardIndex(List<IssueBoard> boards, String name) {
		for (int i=0; i<boards.size(); i++) {
			if (name.equals(boards.get(i).getName()))
				return i;
		}
		return -1;
	}
	
}
