package io.onedev.server.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;

@Entity
@Table(
		indexes={@Index(columnList="g_project_id"), @Index(columnList="g_milestone_id"), @Index(columnList="name")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_project_id", "name"})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class IssueBoard extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;

	@ManyToOne(fetch=FetchType.LAZY)
	private Milestone milestone;
	
	@Column(nullable=false)
	private String name;
	
	private String issueFilter;
	
	@Column(nullable=false)
	private String identifyField;
	
	@Lob
	private ArrayList<String> columns = new ArrayList<>();

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

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
	public ArrayList<String> getColumns() {
		return columns;
	}

	public void setColumns(ArrayList<String> columns) {
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
		Map<String, String> choices = new HashMap<>();
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
	
}
