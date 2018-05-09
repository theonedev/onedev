package io.onedev.server.model.support.issue.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.onedev.server.exception.OneException;
import io.onedev.server.model.support.authorized.ProjectWriters;
import io.onedev.server.model.support.issue.workflow.action.PressButton;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.ValueIsNotSet;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.ValueIsSet;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.ValueSpecification;
import io.onedev.server.util.UsageUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.issuechoiceinput.IssueChoiceInput;
import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.util.inputspec.userchoiceprovider.ProjectReaders;

public class IssueWorkflow implements Serializable, InputContext {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> states = new ArrayList<>();
	
	private transient Map<String, StateSpec> stateMap;
	
	private List<StateTransition> stateTransitions = new ArrayList<>();
	
	private List<InputSpec> fields = new ArrayList<>();

	private boolean reconciled = true;
	
	private transient Map<String, InputSpec> fieldMap;
	
	public IssueWorkflow() {
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
		
		fields.add(type);
		
		ChoiceInput severity = new ChoiceInput();
		severity.setName("Severity");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice minor = new Choice();
		minor.setValue("Minor");
		minor.setColor("#d9ead3");
		choices.add(minor);

		Choice normal = new Choice();
		normal.setValue("Normal");
		normal.setColor("#f4cccc");
		choices.add(normal);

		Choice major = new Choice();
		major.setValue("Major");
		major.setColor("#cc0000");
		choices.add(major);
		
		specifiedChoices.setChoices(choices);
		severity.setChoiceProvider(specifiedChoices);
		
		fields.add(severity);

		UserChoiceInput assignee = new UserChoiceInput();
		assignee.setChoiceProvider(new ProjectReaders());
		assignee.setName("Assignee");
		
		fields.add(assignee);
		
		ChoiceInput resolution = new ChoiceInput();
		resolution.setName("Resolution");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice fixed = new Choice();
		fixed.setValue("Fixed");
		choices.add(fixed);

		Choice wontFix = new Choice();
		wontFix.setValue("Won't Fix");
		choices.add(wontFix);

		Choice duplicated = new Choice();
		duplicated.setValue("Duplicated");
		choices.add(duplicated);
		
		specifiedChoices.setChoices(choices);
		resolution.setChoiceProvider(specifiedChoices);
		
		fields.add(resolution);

		IssueChoiceInput duplicateIssue = new IssueChoiceInput();
		duplicateIssue.setName("Duplicate With");
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("Resolution");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Duplicated"));
		showCondition.setValueMatcher(valueIsOneOf);
		duplicateIssue.setShowCondition(showCondition);
		fields.add(duplicateIssue);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setColor("#f1c232");
		open.setFields(Lists.newArrayList("Type", "Severity"));
		
		states.add(open);
		
		StateSpec assigned = new StateSpec();
		assigned.setName("Assigned");
		assigned.setColor("#5d9d41");
		assigned.setFields(Lists.newArrayList("Assignee"));
		
		states.add(assigned);
		
		StateSpec closed = new StateSpec();
		closed.setColor("#cccccc");
		closed.setName("Closed");
		closed.setFields(Lists.newArrayList("Resolution", "Duplicate With"));
		
		states.add(closed);
		
		StateTransition transition = new StateTransition();
		transition.setFromStates(Lists.newArrayList("Open", "Assigned"));
		transition.setToState("Closed");
		PressButton pressButton = new PressButton();
		pressButton.setName("Close");
		pressButton.setAuthorized(new ProjectWriters());
		transition.setOnAction(pressButton);
		
		stateTransitions.add(transition);
		
		transition = new StateTransition();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Assigned");
		pressButton = new PressButton();
		pressButton.setName("Assign");
		pressButton.setAuthorized(new ProjectWriters());
		transition.setOnAction(pressButton);
		
		stateTransitions.add(transition);
		
		transition = new StateTransition();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		TransitionPrerequisite prerequisite = new TransitionPrerequisite();
		prerequisite.setFieldName("Assignee");
		ValueSpecification specification = new ValueIsNotSet();
		prerequisite.setValueSpecification(specification);
		transition.setPrerequisite(prerequisite);
		pressButton = new PressButton();
		pressButton.setName("Reopen");
		pressButton.setAuthorized(new ProjectWriters());
		transition.setOnAction(pressButton);
		
		stateTransitions.add(transition);
		
		transition = new StateTransition();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Assigned");
		prerequisite = new TransitionPrerequisite();
		prerequisite.setFieldName("Assignee");
		specification = new ValueIsSet();
		prerequisite.setValueSpecification(specification);
		transition.setPrerequisite(prerequisite);
		pressButton = new PressButton();
		pressButton.setName("Reopen");
		pressButton.setAuthorized(new ProjectWriters());
		transition.setOnAction(pressButton);
		
		stateTransitions.add(transition);
	}
	
	public List<StateSpec> getStates() {
		return states;
	}

	public void setStates(List<StateSpec> states) {
		this.states = states;
	}

	public List<StateTransition> getStateTransitions() {
		return stateTransitions;
	}

	public void setStateTransitions(List<StateTransition> stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

	public List<InputSpec> getFields() {
		return fields;
	}

	public void setFields(List<InputSpec> fields) {
		this.fields = fields;
	}

	public boolean isReconciled() {
		return reconciled;
	}

	public void setReconciled(boolean reconciled) {
		this.reconciled = reconciled;
	}

	private Map<String, InputSpec> getFieldMap() {
		if (fieldMap == null) {
			fieldMap = new LinkedHashMap<>();
			for (InputSpec field: getFields())
				fieldMap.put(field.getName(), field);
		}
		return fieldMap;
	}
	
	private Map<String, StateSpec> getStateMap() {
		if (stateMap == null) {
			stateMap = new LinkedHashMap<>();
			for (StateSpec state: getStates())
				stateMap.put(state.getName(), state);
		}
		return stateMap;
	}
	
	@Override
	public List<String> getInputNames() {
		return new ArrayList<>(getFieldMap().keySet());
	}
	
	@Override
	public InputSpec getInput(String inputName) {
		InputSpec field = getFieldMap().get(inputName);
		if (field == null)
			throw new RuntimeException("Unable to find input: " + inputName);
		return field;
	}
	
	public List<String> onDeleteState(String stateName) {
		for (Iterator<StateTransition> it = getStateTransitions().iterator(); it.hasNext();) {
			StateTransition transition = it.next();
			transition.getFromStates().remove(stateName);
			if (transition.getFromStates().isEmpty() || transition.getToState().equals(stateName))
				it.remove();
		}
		return new ArrayList<>();
	}
	
	public void onRenameState(String oldName, String newName) {
		for (StateTransition transition: getStateTransitions()) {
			int index = transition.getFromStates().indexOf(oldName);
			if (index != -1)
				transition.getFromStates().set(index, newName);
			if (transition.getToState().equals(oldName))
				transition.setToState(newName);
		}
	}
	
	@Nullable
	public StateSpec getState(String stateName) {
		return getStateMap().get(stateName);
	}

	public int getStateIndex(String stateName) {
		for (int i=0; i<getStates().size(); i++) {
			if (getStates().get(i).getName().equals(stateName))
				return i;
		}
		return -1;
	}
	
	public int getTransitionIndex(StateTransition transition) {
		for (int i=0; i<getStateTransitions().size(); i++) {
			if (getStateTransitions().get(i) == transition)
				return i;
		}
		return -1;
	}
	
	@Nullable
	public InputSpec getField(String fieldName) {
		return getFieldMap().get(fieldName);
	}

	public int getFieldIndex(String fieldName) {
		for (int i=0; i<getFields().size(); i++) {
			if (getFields().get(i).getName().equals(fieldName))
				return i;
		}
		return -1;
	}
	
	public void onRenameField(String oldName, String newName) {
		for (StateSpec state: getStates()) 
			state.onFieldRename(oldName, newName);
		for (StateTransition transition: getStateTransitions())
			transition.onFieldRename(oldName, newName);
		for (InputSpec field: getFields())
			field.onInputRename(oldName, newName);
	}
	
	public List<String> onDeleteField(String fieldName) {
		List<String> usages = new ArrayList<>();
		for (StateSpec state: getStates())  
			usages.addAll(UsageUtils.prependCategory("Issue state '" + state.getName() + "'", state.onFieldDelete(fieldName)));
		for (StateTransition transition: getStateTransitions()) 
			usages.addAll(UsageUtils.prependCategory("Issue state transition '" + transition + "'", transition.onFieldDelete(fieldName)));
		for (InputSpec field: getFields())
			usages.addAll(UsageUtils.prependCategory("Issue field '" + field.getName() + "'", field.onInputDelete(fieldName)));
		return usages;
	}
	
	public void onRenameUser(String oldName, String newName) {
		for (StateTransition transition: getStateTransitions())
			transition.onRenameUser(oldName, newName);
		for (InputSpec field: getFields())
			field.onRenameUser(oldName, newName);
	}
	
	public List<String> onDeleteUser(String userName) {
		List<String> usages = new ArrayList<>();
		for (InputSpec field: getFields())
			usages.addAll(UsageUtils.prependCategory("Issue field '" + field.getName() + "'" , field.onDeleteUser(userName)));
		for (StateTransition transition: getStateTransitions())
			usages.addAll(UsageUtils.prependCategory("Issue state transition '" + transition + "'", transition.onDeleteUser(userName)));
		return usages;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		for (StateTransition transition: getStateTransitions()) 
			transition.onRenameGroup(oldName, newName);
		for (InputSpec field: getFields())
			field.onRenameGroup(oldName, newName);
	}
	
	public List<String> onDeleteGroup(String groupName) {
		List<String> usages = new ArrayList<>();
		for (InputSpec field: getFields()) 
			usages.addAll(UsageUtils.prependCategory("Issue field '"  + field.getName() + "'", field.onDeleteGroup(groupName)));
		for (StateTransition transition: getStateTransitions())
			usages.addAll(UsageUtils.prependCategory("Issue state transition '" + transition + "'", transition.onDeleteGroup(groupName)));
		return usages;
	}
	
	public StateSpec getInitialState() {
		if (!getStates().isEmpty())
			return getStates().iterator().next();
		else
			throw new OneException("No any issue state is defined");
	}
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
}
