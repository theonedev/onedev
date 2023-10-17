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

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.model.IssueSchedule;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class BoardSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NULL_COLUMN = StringChoiceProvider.SPECIAL_CHOICE_PREFIX + "Null";
	
	private String name;
	
	private String baseQuery;
	
	private String backlogBaseQuery = "\"State\" is \"Open\"";
	
	private String identifyField;
	
	private List<String> columns = new ArrayList<>();
	
	private List<String> displayFields = Lists.newArrayList(Issue.NAME_STATE, IssueSchedule.NAME_MILESTONE);
	
	private List<String> displayLinks = new ArrayList<>();
	
	private List<String> editColumns;
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, placeholder="Not specified", description="Optionally specify a base query to "
			+ "filter/order issues of the board")
	@IssueQuery(withCurrentUserCriteria = true, withCurrentProjectCriteria = true)
	@Nullable
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	@Editable(order=250, placeholder="Not specified", description="Optionally specify a base query to filter/order issues in backlog. "
			+ "Backlog issues are those not associating with current milestone")
	@IssueQuery(withCurrentUserCriteria = true, withCurrentProjectCriteria = true)
	@Nullable
	public String getBacklogBaseQuery() {
		return backlogBaseQuery;
	}

	public void setBacklogBaseQuery(String backlogBaseQuery) {
		this.backlogBaseQuery = backlogBaseQuery;
	}

	@Editable(order=300, description="Specify issue field to identify different columns of the board. "
			+ "Only state and single-valued enumeration field can be used here")
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

	@Editable(order=500, placeholder="Not displaying any fields", description="Specify fields to display in board card")
	@ChoiceProvider("getDisplayFieldChoices")
	public List<String> getDisplayFields() {
		return displayFields;
	}

	public void setDisplayFields(List<String> displayFields) {
		this.displayFields = displayFields;
	}
	
	@Editable(order=600, placeholder="Not displaying any links", description="Specify links to display in board card")
	@ChoiceProvider("getDisplayLinkChoices")
	public List<String> getDisplayLinks() {
		return displayLinks;
	}

	public void setDisplayLinks(List<String> displayLinks) {
		this.displayLinks = displayLinks;
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
	private static List<String> getDisplayFieldChoices() {
		List<String> choices = new ArrayList<>();
		choices.add(Issue.NAME_STATE);
		for (FieldSpec fieldSpec: getIssueSetting().getFieldSpecs()) {
			choices.add(fieldSpec.getName());
		}
		choices.add(IssueSchedule.NAME_MILESTONE);
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getDisplayLinkChoices() {
		List<String> choices = new ArrayList<>();
		for (LinkSpec linkSpec: OneDev.getInstance(LinkSpecManager.class).queryAndSort()) {
			choices.add(linkSpec.getName());
			if (linkSpec.getOpposite() != null)
				choices.add(linkSpec.getOpposite().getName());
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
	
	public Set<String> getUndefinedStates() {
		Set<String> undefinedStates = new HashSet<>();
		undefinedStates.addAll(getBaseQueryUpdater().getUndefinedStates());
		undefinedStates.addAll(getBacklogBaseQueryUpdater().getUndefinedStates());
		if (getIdentifyField().equals(Issue.NAME_STATE)) {
			for (String column: getColumns()) {
				if (getIssueSetting().getStateSpec(column) == null)
					undefinedStates.add(column);
			}
		}
		return undefinedStates;
	}
	
	public Set<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		undefinedFields.addAll(getBaseQueryUpdater().getUndefinedFields());
		undefinedFields.addAll(getBacklogBaseQueryUpdater().getUndefinedFields());
		if (!Issue.NAME_STATE.equals(getIdentifyField())) { 
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getIdentifyField());
			if (fieldSpec == null)
				undefinedFields.add(getIdentifyField());
		}
		for (String displayField: getDisplayFields()) {
			if (!Issue.NAME_STATE.equals(displayField) && !IssueSchedule.NAME_MILESTONE.equals(displayField)) { 
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(displayField);
				if (fieldSpec == null)
					undefinedFields.add(displayField);
			}
		}
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		undefinedFieldValues.addAll(getBaseQueryUpdater().getUndefinedFieldValues());
		undefinedFieldValues.addAll(getBacklogBaseQueryUpdater().getUndefinedFieldValues());
		if (!identifyField.equals(Issue.NAME_STATE)) {
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(identifyField));
			if (specifiedChoices != null) {
				for (String column: getColumns()) {
					if (column != null && !specifiedChoices.getChoiceValues().contains(column)) 
						undefinedFieldValues.add(new UndefinedFieldValue(identifyField, column));
				}
			}
		}
		return undefinedFieldValues;
	}
	
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		getBaseQueryUpdater().fixUndefinedStates(resolutions);
		getBacklogBaseQueryUpdater().fixUndefinedStates(resolutions);
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

	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		getBaseQueryUpdater().fixUndefinedFields(resolutions);
		getBacklogBaseQueryUpdater().fixUndefinedFields(resolutions);
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
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		getBaseQueryUpdater().fixUndefinedFieldValues(resolutions);
		getBacklogBaseQueryUpdater().fixUndefinedFieldValues(resolutions);
		for (Map.Entry<String, UndefinedFieldValuesResolution> resolutionEntry: resolutions.entrySet()) {
			if (resolutionEntry.getKey().equals(getIdentifyField())) {
				getColumns().removeAll(resolutionEntry.getValue().getDeletions());
				for (Map.Entry<String, String> renameEntry: resolutionEntry.getValue().getRenames().entrySet()) 
					ReconcileUtils.renameItem(getColumns(), renameEntry.getKey(), renameEntry.getValue());
			}
		}
		return getColumns().size() >= 2;
	}
	
	public void onRenameUser(GlobalIssueSetting issueSetting, String oldName, String newName) {
		FieldSpec fieldSpec = issueSetting.getFieldSpec(getIdentifyField());
		if (fieldSpec instanceof UserChoiceField) {
			for (int i=0; i<getColumns().size(); i++) {
				if (oldName.equals(getColumns().get(i)))
					getColumns().set(i, newName);
			}
		}
	}

	public boolean onDeleteUser(GlobalIssueSetting issueSetting, String userName) {
		FieldSpec fieldSpec = issueSetting.getFieldSpec(getIdentifyField());
		if (fieldSpec instanceof UserChoiceField) {
			for (Iterator<String> it = getColumns().iterator(); it.hasNext();) {
				if (userName.equals(it.next()))
					it.remove();
			}
		}
		if (getColumns().size() < 2)
			return true;
		else
			return false;
	}

	public IssueQueryUpdater getBaseQueryUpdater() {
		return new IssueQueryUpdater() {

			@Override
			protected Usage getUsage() {
				return new Usage().add("base query");
			}

			@Override
			protected boolean isAllowEmpty() {
				return true;
			}

			@Override
			protected String getIssueQuery() {
				return baseQuery;
			}

			@Override
			protected void setIssueQuery(String issueQuery) {
				baseQuery = issueQuery;
			}
			
		};
	}
	
	public IssueQueryUpdater getBacklogBaseQueryUpdater() {
		return new IssueQueryUpdater() {

			@Override
			protected Usage getUsage() {
				return new Usage().add("backlog base query");
			}

			@Override
			protected boolean isAllowEmpty() {
				return true;
			}

			@Override
			protected String getIssueQuery() {
				return backlogBaseQuery;
			}

			@Override
			protected void setIssueQuery(String issueQuery) {
				backlogBaseQuery = issueQuery;
			}
			
		};
	}
	
}
