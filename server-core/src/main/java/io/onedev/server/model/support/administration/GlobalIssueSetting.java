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

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneException;
import io.onedev.server.issue.BoardSpec;
import io.onedev.server.issue.StateSpec;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.issue.fieldspec.BuildChoiceField;
import io.onedev.server.issue.fieldspec.ChoiceField;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.issue.fieldspec.UserChoiceField;
import io.onedev.server.issue.transitiontrigger.BranchUpdateTrigger;
import io.onedev.server.issue.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.Usage;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.choiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GlobalIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private List<TransitionSpec> transitionSpecs = new ArrayList<>();
	
	private List<FieldSpec> fieldSpecs = new ArrayList<>();
	
	private Collection<String> promptFieldsUponIssueOpen = new HashSet<>();
	
	private List<BoardSpec> boardSpecs = new ArrayList<>();
	
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
		newFeature.setColor("#93c47d");
		choices.add(newFeature);
		
		Choice improvement = new Choice();
		improvement.setValue("Improvement");
		improvement.setColor("#93c47d");
		choices.add(improvement);

		Choice bug = new Choice();
		bug.setValue("Bug");
		bug.setColor("#ea9999");
		choices.add(bug);

		Choice task = new Choice();
		task.setValue("Task");
		task.setColor("#8e7cc3");
		choices.add(task);

		Choice buildFailure = new Choice();
		buildFailure.setValue("Build Failure");
		buildFailure.setColor("#cc0000");
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
		normal.setColor("#6fa8dc");
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

		UserChoiceField assignees = new UserChoiceField();
		assignees.setAllowMultiple(true);
		assignees.setAllowEmpty(true);
		assignees.setNameOfEmptyValue("Not assigned");
		assignees.setName("Assignees");
		
		fieldSpecs.add(assignees);
		
		BuildChoiceField failedBuild = new BuildChoiceField();
		failedBuild.setName("Failed Build");
		failedBuild.setAllowEmpty(true);
		failedBuild.setNameOfEmptyValue("Not specified");
		
		fieldSpecs.add(failedBuild);
		
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("Type");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Build Failure"));
		showCondition.setValueMatcher(valueIsOneOf);
		failedBuild.setShowCondition(showCondition);
		
		BuildChoiceField affectedBuilds = new BuildChoiceField();
		affectedBuilds.setName("Seen Builds");
		affectedBuilds.setAllowMultiple(true);
		
		showCondition = new ShowCondition();
		showCondition.setInputName("Type");
		valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Bug"));
		showCondition.setValueMatcher(valueIsOneOf);
		affectedBuilds.setShowCondition(showCondition);
		
		fieldSpecs.add(affectedBuilds);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setColor("#f0ad4e");
		
		stateSpecs.add(open);
		
		StateSpec committed = new StateSpec();
		committed.setColor("#b4a7d6");
		committed.setName("Committed");
		
		stateSpecs.add(committed);
		
		StateSpec invalid = new StateSpec();
		invalid.setColor("#999999");
		invalid.setName("Invalid");
		
		stateSpecs.add(invalid);
		
		StateSpec closed = new StateSpec();
		closed.setColor("#5cb85c");
		closed.setName("Closed");
		
		stateSpecs.add(closed);
		
		TransitionSpec transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Committed");
		BranchUpdateTrigger branchUpdateTrigger = new BranchUpdateTrigger();
		transition.setTrigger(branchUpdateTrigger);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open", "Committed"));
		transition.setToState("Closed");
		PressButtonTrigger pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Close");
		pressButton.setAuthorizedRoles(Lists.newArrayList("Developer", "Tester"));
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open", "Committed"));
		transition.setToState("Invalid");
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Invalid");
		pressButton.setAuthorizedRoles(Lists.newArrayList("Developer", "Tester"));
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		BuildSuccessfulTrigger buildSuccessful = new BuildSuccessfulTrigger();
		buildSuccessful.setBranches("master");
		buildSuccessful.setIssueQuery("\"Type\" is \"Build Failure\" and (\"Failed Build\" is current or \"Failed Build\" is previous)");
		transition.setTrigger(buildSuccessful);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open", "Committed"));
		transition.setToState("Closed");
		buildSuccessful = new BuildSuccessfulTrigger();
		buildSuccessful.setBranches("master");
		buildSuccessful.setIssueQuery("fixed in current build");
		transition.setTrigger(buildSuccessful);

		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Committed", "Invalid", "Closed"));
		transition.setToState("Open");
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		pressButton.setAuthorizedRoles(Lists.newArrayList("Developer", "Tester"));
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		promptFieldsUponIssueOpen.add("Type");
		promptFieldsUponIssueOpen.add("Priority");
		promptFieldsUponIssueOpen.add("Assignees");
		promptFieldsUponIssueOpen.add("Failed Build");
		promptFieldsUponIssueOpen.add("Seen Builds");
		
		BoardSpec board = new BoardSpec();
		board.setName(Issue.FIELD_STATE);
		board.setIdentifyField(Issue.FIELD_STATE);
		board.setColumns(Lists.newArrayList("Open", "Committed", "Closed"));
		board.setDisplayFields(Lists.newArrayList(Issue.FIELD_STATE, "Type", "Priority", "Assignees"));
		boardSpecs.add(board);
		
		listFields.add("Type");
		listFields.add("Priority");
		listFields.add("Assignees");
		
		namedQueries.add(new NamedIssueQuery("Open", "\"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me & Open", "\"Assignees\" is me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Submitted by me & Open", "submitted by me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me", "\"Assignees\" is me"));
		namedQueries.add(new NamedIssueQuery("Submitted by me", "submitted by me"));
		namedQueries.add(new NamedIssueQuery("Submitted recently", "\"Submit Date\" is after \"last week\""));
		namedQueries.add(new NamedIssueQuery("Updated recently", "\"Update Date\" is after \"last week\""));
		namedQueries.add(new NamedIssueQuery("Open & Critical", "\"State\" is \"Open\" and \"Priority\" is \"Critical\""));
		namedQueries.add(new NamedIssueQuery("Open & Unassigned", "\"State\" is \"Open\" and \"Assignees\" is empty"));
		namedQueries.add(new NamedIssueQuery("Committed", "\"State\" is \"Committed\""));
		namedQueries.add(new NamedIssueQuery("Closed", "\"State\" is \"Closed\""));
		namedQueries.add(new NamedIssueQuery("Invalid", "\"State\" is \"Invalid\""));
		namedQueries.add(new NamedIssueQuery("All", null));
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

	public List<TransitionSpec> getTransitionSpecs() {
		return transitionSpecs;
	}

	public void setTransitionSpecs(List<TransitionSpec> transitionSpecs) {
		this.transitionSpecs = transitionSpecs;
	}

	@Editable
	public List<FieldSpec> getFieldSpecs() {
		return fieldSpecs;
	}

	public void setFieldSpecs(List<FieldSpec> fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
	}

	public Collection<String> getPromptFieldsUponIssueOpen() {
		return promptFieldsUponIssueOpen;
	}

	public void setPromptFieldsUponIssueOpen(Collection<String> promptFieldsUponIssueOpen) {
		this.promptFieldsUponIssueOpen = promptFieldsUponIssueOpen;
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
	
	@Nullable
	public StateSpec getStateSpec(String stateName) {
		return getStateSpecMap().get(stateName);
	}

	@Nullable
	public FieldSpec getFieldSpec(String fieldName) {
		return getFieldSpecMap(null).get(fieldName);
	}
	
	public Collection<String> getUndefinedStates() {
		Collection<String> undefinedStates = new HashSet<>();
		for (TransitionSpec transition: getTransitionSpecs())
			undefinedStates.addAll(transition.getUndefinedStates());
		for (BoardSpec board: getBoardSpecs())
			undefinedStates.addAll(board.getUndefinedStates(null));
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				undefinedStates.addAll(query.getUndefinedStates());
			} catch (Exception e) {
			}
		}
		return undefinedStates;
	}
	
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		for (TransitionSpec transition: getTransitionSpecs())
			undefinedFields.addAll(transition.getUndefinedFields());
		for (FieldSpec field: getFieldSpecs())
			undefinedFields.addAll(field.getUndefinedFields());
		for (BoardSpec board: getBoardSpecs())
			undefinedFields.addAll(board.getUndefinedFields(null));
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				undefinedFields.addAll(query.getUndefinedFields());
			} catch (Exception e) {
			}
		}
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (TransitionSpec transition: getTransitionSpecs())
			undefinedFieldValues.addAll(transition.getUndefinedFieldValues());
		for (FieldSpec field: getFieldSpecs())
			undefinedFieldValues.addAll(field.getUndefinedFieldValues());
		for (BoardSpec board: getBoardSpecs())
			undefinedFieldValues.addAll(board.getUndefinedFieldValues(null));
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				undefinedFieldValues.addAll(query.getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedStates(resolutions))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedStates(null, resolutions))
				it.remove();
		}
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				if (query.fixUndefinedStates(resolutions))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
	}

	public Collection<String> fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedFields(null, resolutions))
				it.remove();
		}
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				if (query.fixUndefinedFields(resolutions))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		Collection<String> derivedDeletions = new HashSet<>();
		for (Iterator<FieldSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			FieldSpec field = it.next();
			if (field.fixUndefinedFields(resolutions)) {
				it.remove();
				derivedDeletions.add(field.getName());
			}
		}

		return derivedDeletions;
	}
	
	public Collection<String> fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) {
			if (it.next().fixUndefinedFieldValues(null, resolutions))
				it.remove();
		}
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true);
				if (query.fixUndefinedFieldValues(resolutions))
					it.remove();
				else
					namedQuery.setQuery(query.toString());
			} catch (Exception e) {
			}
		}
		
		Collection<String> derivedDeletions = new HashSet<>();
		for (Iterator<FieldSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			FieldSpec field = it.next();
			if (field.fixUndefinedFieldValues(resolutions)) {
				it.remove();
				derivedDeletions.add(field.getName());
			}
		}

		return derivedDeletions;
	}
	
	public void onRenameUser(String oldName, String newName) {
		for (FieldSpec field: getFieldSpecs())
			field.onRenameUser(oldName, newName);
		for (BoardSpec board: getBoardSpecs())
			board.onRenameUser(this, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) { 
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
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onRenameRole(oldName, newName);
	}
	
	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		
		for (TransitionSpec transition: getTransitionSpecs())
			usage.add(transition.onDeleteRole(roleName));
		
		return usage.prefix("issue setting");
	}
	
	public StateSpec getInitialStateSpec() {
		if (!getStateSpecs().isEmpty())
			return getStateSpecs().iterator().next();
		else
			throw new OneException("No any issue state is defined");
	}
	
	public List<BoardSpec> getBoardSpecs() {
		return boardSpecs;
	}

	public void setBoardSpecs(List<BoardSpec> boardSpecs) {
		this.boardSpecs = boardSpecs;
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
