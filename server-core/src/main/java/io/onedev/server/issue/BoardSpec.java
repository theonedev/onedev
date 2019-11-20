package io.onedev.server.issue;

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

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.fieldspec.ChoiceField;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.issue.fieldspec.GroupChoiceField;
import io.onedev.server.issue.fieldspec.UserChoiceField;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
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
	
	private List<String> displayFields = Lists.newArrayList(IssueConstants.FIELD_STATE);
	
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
	@IssueQuery
	@Nullable
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	@Editable(order=250, description="Optionally specify a base query to filter/order issues in backlog. "
			+ "Backlog issues are those not associating with any milestones")
	@IssueQuery
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

	@Editable(order=400, description="Specify columns of the board. Each column corresponds to "
			+ "a value of the issue field specified above")
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

	@Editable(order=500, description="Specify fields to display in board card except issue number and title")
	@ChoiceProvider("getDisplayFieldsChoices")
	public List<String> getDisplayFields() {
		return displayFields;
	}

	public void setDisplayFields(List<String> displayFields) {
		this.displayFields = displayFields;
	}

	@SuppressWarnings("unused")
	private static List<String> getIdentifyFieldChoices() {
		List<String> choices = new ArrayList<>();
		choices.add(IssueConstants.FIELD_STATE);
		for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
			if (!fieldSpec.isAllowMultiple() && (fieldSpec instanceof ChoiceField || fieldSpec instanceof UserChoiceField || fieldSpec instanceof GroupChoiceField))
				choices.add(fieldSpec.getName());
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getDisplayFieldsChoices() {
		List<String> choices = new ArrayList<>();
		choices.add(IssueConstants.FIELD_STATE);
		for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
			choices.add(fieldSpec.getName());
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> getColumnChoices() {
		Map<String, String> choices = new LinkedHashMap<>();
		String fieldName = (String) EditContext.get().getInputValue("identifyField");
		if (IssueConstants.FIELD_STATE.equals(fieldName)) {
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
	
	private static IssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public static int getBoardIndex(List<BoardSpec> boards, String name) {
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
				undefinedStates.addAll(io.onedev.server.search.entity.issue.IssueQuery.parse(
						project, getBaseQuery(), false).getUndefinedStates());
			} catch (Exception e) {
			}
		}
		if (getIdentifyField().equals(IssueConstants.FIELD_STATE)) {
			for (String column: getColumns()) {
				if (getIssueSetting().getStateSpec(column) == null)
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
				if (index != -1) {
					if (getColumns().contains(entry.getValue().getNewState())) 
						getColumns().remove(index);
					else 
						getColumns().set(index, entry.getValue().getNewState());
				}
			}
		}
	}
	
	public Set<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (getBaseQuery() != null) {
			try {
				undefinedFields.addAll(io.onedev.server.search.entity.issue.IssueQuery
						.parse(project, getBaseQuery(), false).getUndefinedFields());
			} catch (Exception e) {
			}
		}
		if (!IssueConstants.FIELD_STATE.equals(getIdentifyField())) { 
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getIdentifyField());
			if (fieldSpec == null)
				undefinedFields.add(getIdentifyField());
		}
		for (String displayField: getDisplayFields()) {
			if (!IssueConstants.FIELD_STATE.equals(displayField)) { 
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(displayField);
				if (fieldSpec == null)
					undefinedFields.add(displayField);
			}
		}
		return undefinedFields;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
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
				int index = getDisplayFields().indexOf(entry.getKey());
				if (index != -1) {
					if (getDisplayFields().contains(resolution.getNewField()))
						getDisplayFields().remove(index);
					else
						getDisplayFields().set(index, resolution.getNewField());
				}
			} else {
				getDisplayFields().remove(entry.getKey());
				if (getIdentifyField().equals(entry.getKey())) 
					return true;
			} 
		}				
		return false;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits) {
		for (Map.Entry<String, ValueSetEdit> entry: valueSetEdits.entrySet()) {
			if (onEditFieldValues(entry.getKey(), entry.getValue()))
				return true;
		}
		return false;
	}
	
	public static String getWebSocketObservable(Long projectId) {
		return BoardSpec.class.getName() + ":" + projectId;
	}

	public void onRenameUser(IssueSetting issueSetting, String oldName, String newName) {
		FieldSpec fieldSpec = issueSetting.getFieldSpec(getIdentifyField());
		if (fieldSpec instanceof UserChoiceField) {
			for (int i=0; i<getColumns().size(); i++) {
				if (getColumns().get(i).equals(oldName))
					getColumns().set(i, newName);
			}
		}
	}

	@Nullable
	public Usage onDeleteUser(IssueSetting issueSetting, String userName) {
		FieldSpec fieldSpec = issueSetting.getFieldSpec(getIdentifyField());
		if (fieldSpec instanceof UserChoiceField) {
			for (Iterator<String> it = getColumns().iterator(); it.hasNext();) {
				if (it.next().equals(userName))
					it.remove();
			}
		}
		if (getColumns().size() < 2)
			return null;
		else
			return new Usage();
	}
	
	public void onRenameField(IssueSetting issueSetting, String oldName, String newName) {
		if (getIdentifyField().equals(oldName))
			setIdentifyField(newName);
		int index = getDisplayFields().indexOf(oldName);
		if (index != -1) {
			if (getDisplayFields().contains(newName))
				getDisplayFields().remove(index);
			else
				getDisplayFields().set(index, newName);
		}
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
				query.onRenameField(oldName, newName);
				setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		if (getBacklogBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBacklogBaseQuery(), false);
				query.onRenameField(oldName, newName);
				setBacklogBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}

	public void onRenameState(String oldName, String newName) {
		if (getIdentifyField().equals(IssueConstants.FIELD_STATE)) {
			int index = getColumns().indexOf(oldName);
			if (index != -1) {
				if (getColumns().contains(newName))
					getColumns().remove(index);
				else
					getColumns().set(index, newName);
			}
		}
		
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
				query.onRenameState(oldName, newName);
				setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		if (getBacklogBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBacklogBaseQuery(), false);
				query.onRenameState(oldName, newName);
				setBacklogBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
	}
	
	public boolean onDeleteField(IssueSetting issueSetting, String fieldName) {
		getDisplayFields().remove(fieldName);
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
				if (query.onDeleteField(fieldName))
					setBaseQuery(null);
				else
					setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		if (getBacklogBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBacklogBaseQuery(), false);
				if (query.onDeleteField(fieldName))
					setBacklogBaseQuery(null);
				else
					setBacklogBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		return getIdentifyField().equals(fieldName);
	}

	public boolean onDeleteState(String stateName) {
		if (getIdentifyField().equals(IssueConstants.FIELD_STATE)) 
			getColumns().remove(stateName);
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
				if (query.onDeleteState(stateName))
					setBaseQuery(null);
				else
					setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		if (getBacklogBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBacklogBaseQuery(), false);
				if (query.onDeleteState(stateName))
					setBacklogBaseQuery(null);
				else
					setBacklogBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		return getColumns().size() < 2;
	}

	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		if (fieldName.equals(getIdentifyField())) {
			getColumns().removeAll(valueSetEdit.getDeletions());
			for (Map.Entry<String, String> entry: valueSetEdit.getRenames().entrySet()) {
				int index = getColumns().indexOf(entry.getKey());
				if (index != -1) {
					if (getColumns().contains(entry.getValue()))
						getColumns().remove(index);
					else
						getColumns().set(index, entry.getValue());
				}
			}
		}
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
				if (query.onEditFieldValues(fieldName, valueSetEdit))
					setBaseQuery(null);
				else
					setBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		if (getBacklogBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBacklogBaseQuery(), false);
				if (query.onEditFieldValues(fieldName, valueSetEdit))
					setBacklogBaseQuery(null);
				else
					setBacklogBaseQuery(query.toString());
			} catch (Exception e) {
			}
		}
		return getColumns().size() < 2;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		if (!identifyField.equals(IssueConstants.FIELD_STATE)) {
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(identifyField));
			if (specifiedChoices != null) {
				for (String column: getColumns()) {
					if (column != null && !specifiedChoices.getChoiceValues().contains(column)) 
						undefinedFieldValues.add(new UndefinedFieldValue(identifyField, column));
				}
			}
		}
		if (getBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBaseQuery(), false);
				undefinedFieldValues.addAll(query.getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
		if (getBacklogBaseQuery() != null) {
			try {
				io.onedev.server.search.entity.issue.IssueQuery query = 
						io.onedev.server.search.entity.issue.IssueQuery.parse(null, getBacklogBaseQuery(), false);
				undefinedFieldValues.addAll(query.getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
		return undefinedFieldValues;
	}
	
}
