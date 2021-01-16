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
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.fieldspec.ChoiceField;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.issue.fieldspec.GroupChoiceField;
import io.onedev.server.model.support.issue.fieldspec.UserChoiceField;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;

@Editable
public class BoardSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NULL_COLUMN = StringChoiceProvider.SPECIAL_CHOICE_PREFIX + "Null";
	
	private String name;
	
	private String baseQuery;
	
	private String backlogBaseQuery = "\"State\" is \"Open\"";
	
	private String identifyField;
	
	private List<String> columns = new ArrayList<>();
	
	private List<String> displayFields = Lists.newArrayList(Issue.NAME_STATE);
	
	private List<String> editColumns;

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Optionally specify a base query to filter/order issues of the board")
	@IssueQuery(withCurrentUserCriteria = true, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)
	@Nullable
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	@Editable(order=250, description="Optionally specify a base query to filter/order issues in backlog. "
			+ "Backlog issues are those not associating with any milestones")
	@IssueQuery(withCurrentUserCriteria = true, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)
	@Nullable
	public String getBacklogBaseQuery() {
		return backlogBaseQuery;
	}

	public void setBacklogBaseQuery(String backlogBaseQuery) {
		this.backlogBaseQuery = backlogBaseQuery;
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

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	@Editable(order=400, name="Board Columns", description="Specify columns of the board. "
			+ "Each column corresponds to a value of the issue field specified above")
	@Size(min=2, message="At least two columns need to be defined")
	@ChoiceProvider("getColumnChoices")
	public List<String> getEditColumns() {
		if (editColumns == null)
			editColumns = new ArrayList<>();
		return editColumns;
	}

	public void setEditColumns(List<String> editColumns) {
		this.editColumns = editColumns;
	}
	
	public List<String> getDisplayColumns() {
		List<String> displayColumns = new ArrayList<>();
		for (String column: getColumns()) {
			if (column == null) {
				GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
				FieldSpec field = issueSetting.getFieldSpec(getIdentifyField());
				if (field != null)
					displayColumns.add("<" + HtmlEscape.escapeHtml5(field.getNameOfEmptyValue()) + ">");
			} else {
				displayColumns.add(column);
			}
		}
		return displayColumns;
	}

	@Editable(order=500, description="Specify fields to display in board card except issue number and title")
	@ChoiceProvider("getDisplayFieldsChoices")
	public List<String> getDisplayFields() {
		return displayFields;
	}

	public void setDisplayFields(List<String> displayFields) {
		this.displayFields = displayFields;
	}
	
	public void populateEditColumns() {
		getEditColumns().clear();
		for (String column: getColumns()) 
			getEditColumns().add(column!=null?column:BoardSpec.NULL_COLUMN);
	}
	
	public void populateColumns() {
		getColumns().clear();
		for (String column: getEditColumns()) 
			getColumns().add(column.equals(BoardSpec.NULL_COLUMN)?null:column);
	}

	@SuppressWarnings("unused")
	private static List<String> getIdentifyFieldChoices() {
		List<String> choices = new ArrayList<>();
		choices.add(Issue.NAME_STATE);
		for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
			if (!fieldSpec.isAllowMultiple() && (fieldSpec instanceof ChoiceField || fieldSpec instanceof UserChoiceField || fieldSpec instanceof GroupChoiceField))
				choices.add(fieldSpec.getName());
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getDisplayFieldsChoices() {
		List<String> choices = new ArrayList<>();
		choices.add(Issue.NAME_STATE);
		for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
			choices.add(fieldSpec.getName());
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> getColumnChoices() {
		Map<String, String> choices = new LinkedHashMap<>();
		String fieldName = (String) EditContext.get().getInputValue("identifyField");
		if (Issue.NAME_STATE.equals(fieldName)) {
			for (StateSpec state: getIssueSetting().getStateSpecs())
				choices.put(state.getName(), state.getName());
		} else if (fieldName != null) {
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
			if (fieldSpec != null) {
				for (String each: fieldSpec.getPossibleValues())
					choices.put(each, each);
				if (fieldSpec.isAllowEmpty())
					choices.put(NULL_COLUMN, fieldSpec.getNameOfEmptyValue());
			}
		} 
		return choices;
	}
	
	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public static int getBoardIndex(List<BoardSpec> boards, String name) {
		for (int i=0; i<boards.size(); i++) {
			if (name.equals(boards.get(i).getName()))
				return i;
		}
		return -1;
	}
	
	public Set<String> getUndefinedStates(@Nullable Project project) {
		Set<String> undefinedStates = new HashSet<>();
		undefinedStates.addAll(getUndefinedStates(project, getBaseQuery()));
		undefinedStates.addAll(getUndefinedStates(project, getBacklogBaseQuery()));
		
		if (getIdentifyField().equals(Issue.NAME_STATE)) {
			for (String column: getColumns()) {
				if (getIssueSetting().getStateSpec(column) == null)
					undefinedStates.add(column);
			}
		}
		return undefinedStates;
	}
	
	private Set<String> getUndefinedStates(@Nullable Project project, @Nullable String query) {
		Set<String> undefinedStates = new HashSet<>();
		if (query != null) {
			try {
				undefinedStates.addAll(io.onedev.server.search.entity.issue.IssueQuery.parse(
						project, query, false, true, true, true, true).getUndefinedStates());
			} catch (Exception e) {
			}
		}
		return undefinedStates;
	}
	
	public Set<String> getUndefinedFields(@Nullable Project project) {
		Set<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(getUndefinedFields(project, getBaseQuery()));
		undefinedFields.addAll(getUndefinedFields(project, getBacklogBaseQuery()));
		if (!Issue.NAME_STATE.equals(getIdentifyField())) { 
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getIdentifyField());
			if (fieldSpec == null)
				undefinedFields.add(getIdentifyField());
		}
		for (String displayField: getDisplayFields()) {
			if (!Issue.NAME_STATE.equals(displayField)) { 
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(displayField);
				if (fieldSpec == null)
					undefinedFields.add(displayField);
			}
		}
		return undefinedFields;
	}

	private Set<String> getUndefinedFields(@Nullable Project project, @Nullable String query) {
		Set<String> undefinedFields = new HashSet<>();
		if (query != null) {
			try {
				undefinedFields.addAll(io.onedev.server.search.entity.issue.IssueQuery
						.parse(project, query, false, true, true, true, true).getUndefinedFields());
			} catch (Exception e) {
			}
		}
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues(@Nullable Project project) {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		if (!identifyField.equals(Issue.NAME_STATE)) {
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(identifyField));
			if (specifiedChoices != null) {
				for (String column: getColumns()) {
					if (column != null && !specifiedChoices.getChoiceValues().contains(column)) 
						undefinedFieldValues.add(new UndefinedFieldValue(identifyField, column));
				}
			}
		}
		undefinedFieldValues.addAll(getUndefinedFieldValues(project, getBaseQuery()));
		undefinedFieldValues.addAll(getUndefinedFieldValues(project, getBacklogBaseQuery()));
		return undefinedFieldValues;
	}
	
	private Collection<UndefinedFieldValue> getUndefinedFieldValues(@Nullable Project project, @Nullable String query) {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		if (query != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, query, false, true, true, true, true);
				undefinedFieldValues.addAll(parsedQuery.getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
		return undefinedFieldValues;
	}
	
	public boolean fixUndefinedStates(@Nullable Project project, Map<String, UndefinedStateResolution> resolutions) {
		setBaseQuery(fixUndefinedStates(project, resolutions, getBaseQuery()));
		setBacklogBaseQuery(fixUndefinedStates(project, resolutions, getBacklogBaseQuery()));
		if (getIdentifyField().equals(Issue.NAME_STATE)) {
			for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
				if (entry.getValue().getFixType() == UndefinedStateResolution.FixType.CHANGE_TO_ANOTHER_STATE) 
					ReconcileUtils.renameItem(getColumns(), entry.getKey(), entry.getValue().getNewState());
				else 
					getColumns().remove(entry.getKey());
				if (getColumns().size() < 2)
					return false;
			}
		}
		return true;
	}

	private String fixUndefinedStates(@Nullable Project project, Map<String, UndefinedStateResolution> resolutions, 
			@Nullable String query) {
		if (query != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, query, false, true, true, true, true);
				if (parsedQuery.fixUndefinedStates(resolutions))
					query = parsedQuery.toString();
				else
					query = null;
			} catch (Exception e) {
			}
		}
		return query;
	}
	
	public boolean fixUndefinedFields(@Nullable Project project, Map<String, UndefinedFieldResolution> resolutions) {
		setBaseQuery(fixUndefinedFields(project, resolutions, getBaseQuery()));
		setBacklogBaseQuery(fixUndefinedFields(project, resolutions, getBacklogBaseQuery()));
		
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			UndefinedFieldResolution resolution = entry.getValue();
			if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				if (getIdentifyField().equals(entry.getKey()))
					setIdentifyField(resolution.getNewField());
				ReconcileUtils.renameItem(getDisplayFields(), entry.getKey(), entry.getValue().getNewField());
			} else {
				getDisplayFields().remove(entry.getKey());
				if (getIdentifyField().equals(entry.getKey())) 
					return false;
			} 
		}				
		return true;
	}
	
	private String fixUndefinedFields(@Nullable Project project, Map<String, UndefinedFieldResolution> resolutions, 
			@Nullable String query) {
		if (query != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, query, false, true, true, true, true);
				if (parsedQuery.fixUndefinedFields(resolutions))
					query = parsedQuery.toString();
				else
					query = null;
			} catch (Exception e) {
			}
		}
		return query;
	}
	
	public boolean fixUndefinedFieldValues(@Nullable Project project, Map<String, UndefinedFieldValuesResolution> resolutions) {
		setBaseQuery(fixUndefinedFieldValues(project, resolutions, getBaseQuery()));
		setBacklogBaseQuery(fixUndefinedFieldValues(project, resolutions, getBacklogBaseQuery()));
		for (Map.Entry<String, UndefinedFieldValuesResolution> resolutionEntry: resolutions.entrySet()) {
			if (resolutionEntry.getKey().equals(getIdentifyField())) {
				getColumns().removeAll(resolutionEntry.getValue().getDeletions());
				for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) {
					int index = getColumns().indexOf(renameEntry.getKey());
					if (index != -1) {
						if (getColumns().contains(renameEntry.getValue()))
							getColumns().remove(index);
						else
							getColumns().set(index, renameEntry.getValue());
					}
				}
			}
		}
		return getColumns().size() >= 2;
	}
	
	@Nullable
	private String fixUndefinedFieldValues(@Nullable Project project, Map<String, UndefinedFieldValuesResolution> resolutions, 
			@Nullable String query) {
		if (query != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(project, query, false, true, true, true, true);
				if (parsedQuery.fixUndefinedFieldValues(resolutions))
					query = parsedQuery.toString();
				else
					query = null;
			} catch (Exception e) {
			}
		}
		return query;
	}
	
	public static String getWebSocketObservable(Long projectId) {
		return BoardSpec.class.getName() + ":" + projectId;
	}

	public void onRenameUser(GlobalIssueSetting issueSetting, String oldName, String newName) {
		FieldSpec fieldSpec = issueSetting.getFieldSpec(getIdentifyField());
		if (fieldSpec instanceof UserChoiceField) {
			for (int i=0; i<getColumns().size(); i++) {
				if (getColumns().get(i).equals(oldName))
					getColumns().set(i, newName);
			}
		}
	}

	public boolean onDeleteUser(GlobalIssueSetting issueSetting, String userName) {
		FieldSpec fieldSpec = issueSetting.getFieldSpec(getIdentifyField());
		if (fieldSpec instanceof UserChoiceField) {
			for (Iterator<String> it = getColumns().iterator(); it.hasNext();) {
				if (it.next().equals(userName))
					it.remove();
			}
		}
		if (getColumns().size() < 2)
			return true;
		else
			return false;
	}

}
