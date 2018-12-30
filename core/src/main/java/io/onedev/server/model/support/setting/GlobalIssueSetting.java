package io.onedev.server.model.support.setting;

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
import io.onedev.server.exception.OneException;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.model.support.issue.transitionprerequisite.ValueIsEmpty;
import io.onedev.server.model.support.issue.transitionprerequisite.ValueIsNotEmpty;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.support.usermatcher.CodeWriters;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.OrCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.IssueChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.choiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GlobalIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private List<TransitionSpec> defaultTransitionSpecs = new ArrayList<>();
	
	private List<InputSpec> fieldSpecs = new ArrayList<>();
	
	private Set<String> defaultPromptFieldsUponIssueOpen = new HashSet<>();
	
	private List<BoardSpec> defaultBoardSpecs = new ArrayList<>();
	
	private Set<String> defaultListFields = new HashSet<>();
	
	private List<NamedIssueQuery> defaultQueries = new ArrayList<>();
	
	private boolean reconciled = true;
	
	public GlobalIssueSetting() {
		ChoiceInput type = new ChoiceInput();
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

		specifiedChoices.setChoices(choices);
		type.setChoiceProvider(specifiedChoices);
		
		SpecifiedDefaultValue specifiedDefaultValue = new SpecifiedDefaultValue();
		specifiedDefaultValue.setValue("Bug");
		type.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(type);
		
		ChoiceInput priority = new ChoiceInput();
		priority.setName("Priority");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice minor = new Choice();
		minor.setValue("Minor");
		minor.setColor("#CCCCCC");
		choices.add(minor);

		Choice normal = new Choice();
		normal.setValue("Normal");
		normal.setColor("#9fc5e8");
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

		UserChoiceInput assignee = new UserChoiceInput();
		assignee.setChoiceProvider(new io.onedev.server.util.inputspec.userchoiceinput.choiceprovider.CodeWriters());
		assignee.setName("Assignee");
		
		fieldSpecs.add(assignee);
		
		ChoiceInput resolution = new ChoiceInput();
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

		IssueChoiceInput duplicateWith = new IssueChoiceInput();
		duplicateWith.setName("Duplicate With");
		
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("Resolution");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Duplicated"));
		showCondition.setValueMatcher(valueIsOneOf);
		duplicateWith.setShowCondition(showCondition);
		
		fieldSpecs.add(duplicateWith);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setCategory(StateSpec.Category.OPEN);
		open.setColor("#f0ad4e");
		
		stateSpecs.add(open);
		
		StateSpec assigned = new StateSpec();
		assigned.setName("Assigned");
		assigned.setCategory(StateSpec.Category.OPEN);
		assigned.setColor("#6fa8dc");
		
		stateSpecs.add(assigned);
		
		StateSpec closed = new StateSpec();
		closed.setColor("#5cb85c");
		closed.setName("Closed");
		closed.setCategory(StateSpec.Category.CLOSED);
		
		stateSpecs.add(closed);
		
		TransitionSpec transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Assigned");
		PressButtonTrigger pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Assign");
		pressButton.setAuthorized(new CodeWriters());
		pressButton.setPromptFields(Lists.newArrayList("Assignee"));
		transition.setTrigger(pressButton);
		
		defaultTransitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open", "Assigned"));
		transition.setToState("Closed");
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Close");
		pressButton.setAuthorized(new CodeWriters());
		pressButton.setPromptFields(Lists.newArrayList("Resolution", "Duplicate With"));
		transition.setTrigger(pressButton);
		
		defaultTransitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		transition.setPrerequisite(new TransitionPrerequisite());
		transition.getPrerequisite().setInputName("Assignee");
		transition.getPrerequisite().setValueMatcher(new ValueIsEmpty());
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		transition.setRemoveFields(Lists.newArrayList("Resolution", "Duplicate With"));
		pressButton.setAuthorized(new CodeWriters());
		transition.setTrigger(pressButton);
		
		defaultTransitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Assigned");
		transition.setPrerequisite(new TransitionPrerequisite());
		transition.getPrerequisite().setInputName("Assignee");
		transition.getPrerequisite().setValueMatcher(new ValueIsNotEmpty());
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		transition.setRemoveFields(Lists.newArrayList("Resolution", "Duplicate With"));
		pressButton.setAuthorized(new CodeWriters());
		transition.setTrigger(pressButton);
		
		defaultTransitionSpecs.add(transition);
		
		defaultPromptFieldsUponIssueOpen.add("Type");
		defaultPromptFieldsUponIssueOpen.add("Priority");
		
		BoardSpec board = new BoardSpec();
		board.setName(IssueConstants.FIELD_STATE);
		board.setIdentifyField(IssueConstants.FIELD_STATE);
		board.setColumns(Lists.newArrayList("Open", "Assigned", "Closed"));
		board.setDisplayFields(Lists.newArrayList(IssueConstants.FIELD_STATE, "Type", "Priority", "Assignee", "Resolution", "Duplicate With"));
		defaultBoardSpecs.add(board);
		
		defaultListFields.add("Type");
		defaultListFields.add("Priority");
		defaultListFields.add("Assignee");
		
		defaultQueries.add(new NamedIssueQuery("Outstanding", "outstanding"));
		defaultQueries.add(new NamedIssueQuery("My outstanding", "outstanding and mine"));
		defaultQueries.add(new NamedIssueQuery("Submitted recently", "\"Submit Date\" is after \"last week\""));
		defaultQueries.add(new NamedIssueQuery("Updated recently", "\"Update Date\" is after \"last week\""));
		defaultQueries.add(new NamedIssueQuery("Submitted by me", "submitted by me"));
		defaultQueries.add(new NamedIssueQuery("Assigned to me", "\"Assignee\" is me"));
		defaultQueries.add(new NamedIssueQuery("Critical outstanding", "outstanding and \"Priority\" is \"Critical\""));
		defaultQueries.add(new NamedIssueQuery("Unassigned outstanding", "outstanding and \"Assignee\" is empty"));
		defaultQueries.add(new NamedIssueQuery("Closed", "closed"));
		defaultQueries.add(new NamedIssueQuery("All", "all"));
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
	public List<InputSpec> getFieldSpecs() {
		return fieldSpecs;
	}

	public void setFieldSpecs(List<InputSpec> fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
	}

	public Set<String> getDefaultPromptFieldsUponIssueOpen() {
		return defaultPromptFieldsUponIssueOpen;
	}

	public void setDefaultPromptFieldsUponIssueOpen(Set<String> defaultPromptFieldsUponIssueOpen) {
		this.defaultPromptFieldsUponIssueOpen = defaultPromptFieldsUponIssueOpen;
	}

	public boolean isReconciled() {
		return reconciled;
	}

	public void setReconciled(boolean reconciled) {
		this.reconciled = reconciled;
	}

	private Map<String, InputSpec> getFieldSpecMap() {
		// Do not use cache here as we may change fieldSpecs while editing IssueSetting
		Map<String, InputSpec> fieldSpecMap = new LinkedHashMap<>();
		for (InputSpec field: getFieldSpecs())
			fieldSpecMap.put(field.getName(), field);
		return fieldSpecMap;
	}
	
	private Map<String, StateSpec> getStateSpecMap() {
		// Do not use cache here as we may change fieldSpecs while editing IssueSetting
		Map<String, StateSpec> stateSpecMap = new LinkedHashMap<>();
		for (StateSpec state: getStateSpecs())
			stateSpecMap.put(state.getName(), state);
		return stateSpecMap;
	}
	
	public List<String> getFieldNames() {
		return new ArrayList<>(getFieldSpecMap().keySet());
	}
	
	public void onDeleteState(String stateName) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			transition.getFromStates().remove(stateName);
			if (transition.getFromStates().isEmpty() || transition.getToState().equals(stateName))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().onDeleteState(stateName))
				it.remove();
		}
		
		for (Iterator<NamedIssueQuery> it = getDefaultQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
				if (query.onDeleteState(stateName))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}
	
	public void onRenameState(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs()) {
			int index = transition.getFromStates().indexOf(oldName);
			if (index != -1)
				transition.getFromStates().set(index, newName);
			if (transition.getToState().equals(oldName))
				transition.setToState(newName);
		}
		for (BoardSpec board: getDefaultBoardSpecs())
			board.onRenameState(oldName, newName);
		for (NamedIssueQuery namedQuery: getDefaultQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
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
	public InputSpec getFieldSpec(String fieldName) {
		return getFieldSpecMap().get(fieldName);
	}

	public void onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onEditFieldValues(fieldName, valueSetEdit))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().onEditFieldValues(fieldName, valueSetEdit))
				it.remove();
		}
		
		for (Iterator<NamedIssueQuery> it = getDefaultQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
				if (query.onEditFieldValues(fieldName, valueSetEdit))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		Set<String> deletedFields = new HashSet<>();
		for (Iterator<InputSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			InputSpec field = it.next();
			if (field.onEditInputValues(fieldName, valueSetEdit)) {
				it.remove();
				deletedFields.add(field.getName());
			}
		}
		for (String deletedField: deletedFields)
			onDeleteField(deletedField);
	}
	
	public void onRenameField(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs())
			transition.onRenameField(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameInput(oldName, newName);
		for (BoardSpec board: getDefaultBoardSpecs())
			board.onRenameField(this, oldName, newName);
		for (NamedIssueQuery namedQuery: getDefaultQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
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
			if (it.next().onDeleteField(this, fieldName))
				it.remove();
		}

		for (Iterator<NamedIssueQuery> it = getDefaultQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false);
				if (query.onDeleteField(fieldName))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		Set<String> deletedFields = new HashSet<>();
		for (Iterator<InputSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			InputSpec field = it.next();
			if (field.onDeleteInput(fieldName)) {
				it.remove();
				deletedFields.add(field.getName());
			}
		}
		for (String deletedField: deletedFields)
			onDeleteField(deletedField);
	}
	
	public void onRenameUser(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs())
			transition.onRenameUser(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameUser(oldName, newName);
		for (BoardSpec board: getDefaultBoardSpecs())
			board.onRenameUser(this, oldName, newName);
	}
	
	public void onDeleteUser(String userName) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onDeleteUser(userName))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getDefaultBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().onDeleteUser(this, userName))
				it.remove();
		}
		
		Set<String> deletedFields = new HashSet<>();
		for (Iterator<InputSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			InputSpec field = it.next();
			if (field.onDeleteUser(userName)) {
				it.remove();
				deletedFields.add(field.getName());
			}
		}
		for (String deletedField: deletedFields)
			onDeleteField(deletedField);
	}
	
	public void onRenameGroup(String oldName, String newName) {
		for (TransitionSpec transition: getDefaultTransitionSpecs()) 
			transition.onRenameGroup(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameGroup(oldName, newName);
	}
	
	public void onDeleteGroup(String groupName) {
		for (Iterator<TransitionSpec> it = getDefaultTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onDeleteGroup(groupName))
				it.remove();
		}
		Set<String> deletedFields = new HashSet<>();
		for (Iterator<InputSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			InputSpec field = it.next();
			if (field.onDeleteGroup(groupName)) {
				it.remove();
				deletedFields.add(field.getName());
			}
		}
		for (String deletedField: deletedFields)
			onDeleteField(deletedField);
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
		return new OrCriteria(criterias);
	}

	public List<BoardSpec> getDefaultBoardSpecs() {
		return defaultBoardSpecs;
	}

	public void setDefaultBoardSpecs(List<BoardSpec> defaultBoardSpecs) {
		this.defaultBoardSpecs = defaultBoardSpecs;
	}

	public Set<String> getDefaultListFields() {
		return defaultListFields;
	}

	public void setDefaultListFields(Set<String> defaultListFields) {
		this.defaultListFields = defaultListFields;
	}
	
	@Editable(order=300, description="Define default issue queries for all projects")
	public List<NamedIssueQuery> getDefaultQueries() {
		return defaultQueries;
	}

	public void setDefaultQueries(List<NamedIssueQuery> defaultQueries) {
		this.defaultQueries = defaultQueries;
	}
	
}
