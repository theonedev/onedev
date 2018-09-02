package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.teamchoiceinput.TeamChoiceInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValueResolution;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedStateResolution;

@Editable
public class IssueBoard implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String baseQuery;
	
	private String backlogBaseQuery = "outstanding";
	
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

	@Editable(order=200, description="Optionally specify a base query to filter/order issues of the board")
	@IssueQuery
	@Nullable
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	@Editable(order=250, description="Optionally specify a base query to filter/order issues in backlog")
	@IssueQuery
	@Nullable
	public String getBacklogBaseQuery() {
		return backlogBaseQuery;
	}

	public void setBacklogBaseQuery(String backlogBaseQuery) {
		this.backlogBaseQuery = backlogBaseQuery;
	}

	@Editable(order=300, description="Specify issue field to identify different lists of the board")
	@ChoiceProvider("getIdentifyFieldChoices")
	@NotEmpty
	public String getIdentifyField() {
		return identifyField;
	}

	public void setIdentifyField(String identifyField) {
		this.identifyField = identifyField;
	}

	@Editable(order=400, description="Specify lists of the board. Each list corresponds to "
			+ "a value of the issue field specified above")
	@Size(min=2, message="At least two lists need to be defined")
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
		choices.add(IssueConstants.FIELD_STATE);
		for (InputSpec fieldSpec: project.getIssueWorkflow().getFieldSpecs()) {
			if (!fieldSpec.isAllowMultiple() && (fieldSpec instanceof ChoiceInput || fieldSpec instanceof UserChoiceInput || fieldSpec instanceof TeamChoiceInput))
				choices.add(fieldSpec.getName());
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> getColumnChoices() {
		Map<String, String> choices = new LinkedHashMap<>();
		Project project = OneContext.get().getProject();
		String fieldName = (String) OneContext.get().getEditContext().getInputValue("identifyField");
		if (IssueConstants.FIELD_STATE.equals(fieldName)) {
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
	
	public Set<String> getUndefinedStates(Project project) {
		Set<String> undefinedStates = new HashSet<>();
		if (getBaseQuery() != null) {
			try {
				undefinedStates.addAll(io.onedev.server.search.entity.issue.IssueQuery.parse(project, getBaseQuery(), false).getUndefinedStates(project));
			} catch (Exception e) {
			}
		}
		if (getIdentifyField().equals(IssueConstants.FIELD_STATE)) {
			for (String column: getColumns()) {
				if (project.getIssueWorkflow().getStateSpec(column) == null)
					undefinedStates.add(column);
			}
		}
		return undefinedStates;
	}
	
	public void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions) {
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, getBaseQuery(), false);
				for (Map.Entry<String, UndefinedStateResolution> resolutionEntry: resolutions.entrySet())
					query.onRenameState(resolutionEntry.getKey(), resolutionEntry.getValue().getNewState());
				setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		if (getIdentifyField().equals(IssueConstants.FIELD_STATE)) {
			for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
				int index = getColumns().indexOf(entry.getKey());
				if (index != -1)
					getColumns().set(index, entry.getValue().getNewState());
			}
		}
	}
	
	public Set<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (getBaseQuery() != null) {
			try {
				undefinedFields.addAll(io.onedev.server.search.entity.issue.IssueQuery.parse(project, getBaseQuery(), false).getUndefinedFields(project));
			} catch (Exception e) {
			}
		}
		if (!IssueConstants.FIELD_STATE.equals(getIdentifyField())) { 
			InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(getIdentifyField());
			if (fieldSpec == null)
				undefinedFields.add(getIdentifyField());
		}
		return undefinedFields;
	}
	
	public boolean fixUndefinedFields(Project project, Map<String, UndefinedFieldResolution> resolutions) {
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, getBaseQuery(), false);
				boolean remove = false;
				for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
					UndefinedFieldResolution resolution = entry.getValue();
					if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
						query.onRenameField(entry.getKey(), resolution.getNewField());
					} else if (query.onDeleteField(entry.getKey())) {
						remove = true;
						break;
					}
				}				
				if (remove)
					setBaseQuery(null);
				else
					setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			UndefinedFieldResolution resolution = entry.getValue();
			if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				if (getIdentifyField().equals(entry.getKey()))
					setIdentifyField(resolution.getNewField());
			} else if (getIdentifyField().equals(entry.getKey())) {
				return true;
			}
		}				
		return false;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		Set<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		OneContext.push(new OneContext() {

			@Override
			public Project getProject() {
				return project;
			}

			@Override
			public EditContext getEditContext(int level) {
				return new EditContext() {

					@Override
					public Object getInputValue(String name) {
						return null;
					}
					
				};
			}

			@Override
			public InputContext getInputContext() {
				throw new UnsupportedOperationException();
			}
			
		});
		try {
			if (getBaseQuery() != null) {
				try {
					undefinedFieldValues.addAll(io.onedev.server.search.entity.issue.IssueQuery.parse(project, getBaseQuery(), true).getUndefinedFieldValues(project));
				} catch (Exception e) {
				}
			}

			if (!getIdentifyField().equals(IssueConstants.FIELD_STATE)) {
				for (String column: getColumns()) {
					InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(getIdentifyField());
					List<String> choices = fieldSpec.getPossibleValues();
					if (!choices.contains(column))
						undefinedFieldValues.add(new UndefinedFieldValue(getIdentifyField(), column));
				}
			}
			
			return undefinedFieldValues;
		} finally {
			OneContext.pop();
		}
	}
	
	public boolean fixUndefinedFieldValues(Project project, Map<UndefinedFieldValue, UndefinedFieldValueResolution> resolutions) {
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, getBaseQuery(), true);
				boolean remove = false;
				for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> entry: resolutions.entrySet()) {
					UndefinedFieldValueResolution resolution = entry.getValue();
					if (resolution.getFixType() == UndefinedFieldValueResolution.FixType.CHANGE_TO_ANOTHER_VALUE) {
						query.onRenameFieldValue(entry.getKey().getFieldName(), entry.getKey().getFieldValue(), 
								entry.getValue().getNewValue());
					} else if (query.onDeleteFieldValue(entry.getKey().getFieldName(), entry.getKey().getFieldValue())) {
						remove = true;
						break;
					}
				}				
				if (remove)
					setBaseQuery(null);
				else
					setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}

		for (Iterator<String> it = getColumns().iterator(); it.hasNext();) {
			String column = it.next();
			for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> entry: resolutions.entrySet()) {
				UndefinedFieldValueResolution resolution = entry.getValue();
				if (resolution.getFixType() == UndefinedFieldValueResolution.FixType.DELETE_THIS_VALUE) {
					if (entry.getKey().getFieldName().equals(getIdentifyField()) 
							&& entry.getKey().getFieldValue().equals(column)) {
						it.remove();
					}
				} 
			}				
		}
		
		if (getColumns().size() < 2)
			return true;
		
		for (int i=0; i<getColumns().size(); i++) {
			String column = getColumns().get(i);
			for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> entry: resolutions.entrySet()) {
				UndefinedFieldValueResolution resolution = entry.getValue();
				if (resolution.getFixType() == UndefinedFieldValueResolution.FixType.CHANGE_TO_ANOTHER_VALUE) {
					if (entry.getKey().getFieldName().equals(getIdentifyField()) 
							&& entry.getKey().getFieldValue().equals(column)) {
						getColumns().set(i, resolution.getNewValue());
					}
				} 
			}				
		}
		
		return false;
	}
	
}
