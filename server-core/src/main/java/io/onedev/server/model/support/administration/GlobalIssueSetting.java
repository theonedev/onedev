package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.issue.BoardSpec;
import io.onedev.server.issue.StateSpec;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.issue.fieldspec.BuildChoiceField;
import io.onedev.server.issue.fieldspec.ChoiceField;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.issue.fieldspec.IssueChoiceField;
import io.onedev.server.issue.fieldspec.UserChoiceField;
import io.onedev.server.issue.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.OrIssueCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.choiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.query.IssueQueryConstants;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GlobalIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private List<TransitionSpec> defaultTransitionSpecs = new ArrayList<>();
	
	private List<FieldSpec> fieldSpecs = new ArrayList<>();
	
	private Collection<String> defaultPromptFieldsUponIssueOpen = new HashSet<>();
	
	private List<BoardSpec> defaultBoardSpecs = new ArrayList<>();
	
	private List<String> listFields = new ArrayList<>();
	
	private List<NamedIssueQuery> namedQueries = new ArrayList<>();
	
	private boolean reconciled = true;
	
	public GlobalIssueSetting() {
		ChoiceField type = new ChoiceField();
		type.setName("Type");
		SpecifiedChoices specifiedChoices = new SpecifiedChoices();

		List<Choice> choices = new ArrayList<>(); 
		Choice newFeature = new Choice();
		newFeature.setValue("New Feature");
		choices.add(newFeature);
		
		Choice improvement = new Choice();
		improvement.setValue("Improvement");
		choices.add(improvement);

		Choice bug = new Choice();
		bug.setValue("Bug");
		choices.add(bug);

		Choice task = new Choice();
		task.setValue("Task");
		choices.add(task);

		Choice buildFailure = new Choice();
		buildFailure.setValue("Build Failure");
		choices.add(buildFailure);
		
		specifiedChoices.setChoices(choices);
		type.setChoiceProvider(specifiedChoices);
		
		SpecifiedDefaultValue specifiedDefaultValue = new SpecifiedDefaultValue();
		specifiedDefaultValue.setValue("Bug");
		type.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(type);
		
		ChoiceField priority = new ChoiceField();
		priority.setName("Priority");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice minor = new Choice();
		minor.setValue("Minor");
		minor.setColor("#CCCCCC");
		choices.add(minor);

		Choice normal = new Choice();
		normal.setValue("Normal");
		normal.setColor("#0d87e9");
		choices.add(normal);

		Choice major = new Choice();
		major.setValue("Major");
		major.setColor("#ff9900");
		choices.add(major);
		
		Choice critical = new Choice();
		critical.setValue("Critical");
		critical.setColor("#cc0000");
		choices.add(critical);
		
		specifiedChoices.setChoices(choices);
		priority.setChoiceProvider(specifiedChoices);
		
		specifiedDefaultValue = new SpecifiedDefaultValue();
		specifiedDefaultValue.setValue("Normal");
		priority.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(priority);

		UserChoiceField assignee = new UserChoiceField();
		assignee.setAllowEmpty(true);
		assignee.setNameOfEmptyValue("Not assigned");
		assignee.setName("Assignee");
		
		fieldSpecs.add(assignee);
		
		BuildChoiceField build = new BuildChoiceField();
		build.setName("Build");
		build.setAllowEmpty(true);
		build.setNameOfEmptyValue("No build specified");
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("Type");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Build Failure"));
		showCondition.setValueMatcher(valueIsOneOf);
		build.setShowCondition(showCondition);
		
		fieldSpecs.add(build);
		
		ChoiceField resolution = new ChoiceField();
		resolution.setName("Resolution");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice fixed = new Choice();
		fixed.setValue("Fixed");
		choices.add(fixed);

		Choice wontFix = new Choice();
		wontFix.setValue("Invalid");
		choices.add(wontFix);

		Choice duplicated = new Choice();
		duplicated.setValue("Duplicated");
		choices.add(duplicated);
		
		specifiedChoices.setChoices(choices);
		resolution.setChoiceProvider(specifiedChoices);
		
		specifiedDefaultValue = new SpecifiedDefaultValue();
		specifiedDefaultValue.setValue("Fixed");
		resolution.setDefaultValueProvider(specifiedDefaultValue);

		fieldSpecs.add(resolution);

		IssueChoiceField duplicateWith = new IssueChoiceField();
		duplicateWith.setName("Duplicate With");
		
		showCondition = new ShowCondition();
		showCondition.setInputName("Resolution");
		valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Duplicated"));
		showCondition.setValueMatcher(valueIsOneOf);
		duplicateWith.setShowCondition(showCondition);
		
		fieldSpecs.add(duplicateWith);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setCategory(StateSpec.Category.OPEN);
		open.setColor("#f0ad4e");
		
		stateSpecs.add(open);
		
		StateSpec closed = new StateSpec();
		closed.setColor("#5cb85c");
		closed.setName("Closed");
		closed.setCategory(StateSpec.Category.CLOSED);
		
		stateSpecs.add(closed);
		
		TransitionSpec transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		PressButtonTrigger pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Close");
		pressButton.setAuthorizedRoles(Lists.newArrayList("Developer", "Tester"));
		pressButton.setPromptFields(Lists.newArrayList("Resolution", "Duplicate With"));
		transition.setTrigger(pressButton);
		
		defaultTransitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		BuildSuccessfulTrigger buildSuccessful = new BuildSuccessfulTrigger();
		buildSuccessful.setBranches("master");
		buildSuccessful.setIssueQuery("\"Type\" is \"Build Failure\" and (\"Build\" is current or \"Build\" is previous)");
		transition.setTrigger(buildSuccessful);
		
		defaultTransitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		buildSuccessful = new BuildSuccessfulTrigger();
		buildSuccessful.setBranches("master");
		buildSuccessful.setIssueQuery("fixed in current build");
		transition.setTrigger(buildSuccessful);

		defaultTransitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		transition.setRemoveFields(Lists.newArrayList("Resolution", "Duplicate With"));
		pressButton.setAuthorizedRoles(Lists.newArrayList("Developer", "Tester"));
		transition.setTrigger(pressButton);
		
		defaultTransitionSpecs.add(transition);
		
		defaultPromptFieldsUponIssueOpen.add("Type");
		defaultPromptFieldsUponIssueOpen.add("Priority");
		defaultPromptFieldsUponIssueOpen.add("Assignee");
		defaultPromptFieldsUponIssueOpen.add("Build");
		
		BoardSpec board = new BoardSpec();
		board.setName(IssueQueryConstants.FIELD_STATE);
		board.setIdentifyField(IssueQueryConstants.FIELD_STATE);
		board.setColumns(Lists.newArrayList("Open", "Closed"));
		board.setDisplayFields(Lists.newArrayList(IssueQueryConstants.FIELD_STATE, "Type", "Priority", "Assignee", "Resolution", "Duplicate With"));
		defaultBoardSpecs.add(board);
		
		listFields.add("Type");
		listFields.add("Priority");
		listFields.add("Assignee");
		
		namedQueries.add(new NamedIssueQuery("Open", "\"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me & Open", "\"Assignee\" is me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Submitted by me & Open", "submitted by me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me", "\"Assignee\" is me"));
		namedQueries.add(new NamedIssueQuery("Submitted by me", "submitted by me"));
		namedQueries.add(new NamedIssueQuery("Submitted recently", "\"Submit Date\" is after \"last week\""));
		namedQueries.add(new NamedIssueQuery("Updated recently", "\"Update Date\" is after \"last week\""));
		namedQueries.add(new NamedIssueQuery("Open & Critical", "\"State\" is \"Open\" and \"Priority\" is \"Critical\""));
		namedQueries.add(new NamedIssueQuery("Open & Unassigned", "\"State\" is \"Open\" and \"Assignee\" is empty"));
		namedQueries.add(new NamedIssueQuery("Closed", "\"State\" is \"Closed\""));
		namedQueries.add(new NamedIssueQuery("All", "all"));
	}
	
	public List<String> sortFieldNames(Collection<String> fieldNames) {
		List<String> sorted = new ArrayList<>(fieldNames);
		List<String> allFieldNames = getFieldNames();
		Collections.sort(sorted, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return allFieldNames.indexOf(o1) - allFieldNames.indexOf(o2);
			}
			
		});
		return sorted;
	}
	
	public List<StateSpec> getStateSpecs() {
		return stateSpecs;
	}

	public void setStateSpecs(List<StateSpec> stateSpecs) {
		this.stateSpecs = stateSpecs;
	}

	public List<TransitionSpec> getDefaultTransitionSpecs() {
		return defaultTransitionSpecs;
	}

	public void setDefaultTransitionSpecs(List<TransitionSpec> defaultStateTransitions) {
		this.defaultTransitionSpecs = defaultStateTransitions;
	}

	@Editable
	public List<FieldSpec> getFieldSpecs() {
		return fieldSpecs;
	}

	public void setFieldSpecs(List<FieldSpec> fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
	}

	public Collection<String> getDefaultPromptFieldsUponIssueOpen() {
		return defaultPromptFieldsUponIssueOpen;
	}

	public void setDefaultPromptFieldsUponIssueOpen(Collection<String> defaultPromptFieldsUponIssueOpen) {
		this.defaultPromptFieldsUponIssueOpen = defaultPromptFieldsUponIssueOpen;
	}

	public boolean isReconciled() {
		return reconciled;
	}

	public void setReconciled(boolean reconciled) {
		this.reconciled = reconciled;
	}

	public Map<String, FieldSpec> getFieldSpecMap(@Nullable Collection<String> fieldNames) {
		Map<String, FieldSpec> fieldSpecMap = new LinkedHashMap<>();
		for (FieldSpec fieldSpec: getFieldSpecs()) {
			if (fieldNames == null || fieldNames.contains(fieldSpec.getName()))
				fieldSpecMap.put(fieldSpec.getName(), fieldSpec);
		}
		return fieldSpecMap;
	}
	
	public Map<String, StateSpec> getStateSpecMap() {
		Map<String, StateSpec> stateSpecMap = new LinkedHashMap<>();
		for (StateSpec state: getStateSpecs())
			stateSpecMap.put(state.getName(), state);
		return stateSpecMap;
	}
	
	public List<String> getFieldNames() {
		return new ArrayList<>(getFieldSpecMap(null).keySet());
	}
	
	public void onDeleteState(String stateName) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) {
			if (it.next().onDeleteState(stateName))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().onDeleteState(stateName))
				it.remove();
		}
		
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				if (query.onDeleteState(stateName))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}
	
	public void onRenameState(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs())
			transition.onRenameState(oldName, newName);
		for (BoardSpec board: getDefaultBoardSpecs())
			board.onRenameState(oldName, newName);
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				query.onRenameState(oldName, newName);
				namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}
	
	@Nullable
	public StateSpec getStateSpec(String stateName) {
		return getStateSpecMap().get(stateName);
	}

	@Nullable
	public FieldSpec getFieldSpec(String fieldName) {
		return getFieldSpecMap(null).get(fieldName);
	}

	public void onEditFieldValues(@Nullable Project project, String fieldName, ValueSetEdit valueSetEdit) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onEditFieldValues(fieldName, valueSetEdit))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().onEditFieldValues(project, fieldName, valueSetEdit))
				it.remove();
		}
		
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				if (query.onEditFieldValues(fieldName, valueSetEdit))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		Set<String> deletedFields = new HashSet<>();
		for (Iterator<FieldSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			FieldSpec field = it.next();
			if (field.onEditInputValues(fieldName, valueSetEdit)) {
				it.remove();
				deletedFields.add(field.getName());
			}
		}
		for (String deletedField: deletedFields) {
			onDeleteField(deletedField);
			OneDev.getInstance(RoleManager.class).onDeleteIssueField(deletedField);
		}
	}
	
	public void onRenameField(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs())
			transition.onRenameField(oldName, newName);
		for (FieldSpec field: getFieldSpecs())
			field.onRenameInput(oldName, newName);
		for (BoardSpec board: getDefaultBoardSpecs())
			board.onRenameField(oldName, newName);
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				query.onRenameField(oldName, newName);
				namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}
	
	public void onDeleteField(String fieldName) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) { 
			if (it.next().onDeleteField(fieldName))
				it.remove();
		}
		
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().onDeleteField(fieldName))
				it.remove();
		}

		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				if (query.onDeleteField(fieldName))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		for (FieldSpec field: getFieldSpecs())
			field.onDeleteInput(fieldName);
	}
	
	public void onRenameUser(String oldName, String newName) {
		for (FieldSpec field: getFieldSpecs())
			field.onRenameUser(oldName, newName);
		for (BoardSpec board: getDefaultBoardSpecs())
			board.onRenameUser(this, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) { 
			if (it.next().onDeleteUser(this, userName))
				it.remove();
		}
		for (FieldSpec field: getFieldSpecs())
			usage.add(field.onDeleteUser(userName));
		
		return usage.prefix("issue setting");
	}
	
	public void onRenameGroup(String oldName, String newName) {
		for (FieldSpec field: getFieldSpecs())
			field.onRenameGroup(oldName, newName);
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		
		for (FieldSpec field: getFieldSpecs())
			usage.add(field.onDeleteGroup(groupName));
		
		return usage.prefix("issue setting");
	}
	
	public void onRenameRole(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs())
			transition.onRenameRole(oldName, newName);
	}
	
	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		
		for (TransitionSpec transition: getDefaultTransitionSpecs())
			usage.add(transition.onDeleteRole(roleName));
		
		return usage.prefix("issue setting");
	}
	
	public StateSpec getInitialStateSpec() {
		if (!getStateSpecs().isEmpty())
			return getStateSpecs().iterator().next();
		else
			throw new OneException("No any issue state is defined");
	}
	
	public IssueCriteria getCategoryCriteria(StateSpec.Category category) {
		List<IssueCriteria> criterias = new ArrayList<>();
		for (StateSpec state: getStateSpecs()) {
			if (category == state.getCategory())
				criterias.add(new StateCriteria(state.getName()));
		}
		return new OrIssueCriteria(criterias);
	}

	public List<BoardSpec> getDefaultBoardSpecs() {
		return defaultBoardSpecs;
	}

	public void setDefaultBoardSpecs(List<BoardSpec> defaultBoardSpecs) {
		this.defaultBoardSpecs = defaultBoardSpecs;
	}

	public List<String> getListFields() {
		return listFields;
	}

	public void setListFields(List<String> listFields) {
		this.listFields = listFields;
	}
	
	public List<NamedIssueQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedIssueQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedIssueQuery getNamedQuery(String name) {
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
