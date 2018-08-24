package io.onedev.server.model.support.issue.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.onedev.server.entityquery.issue.IssueCriteria;
import io.onedev.server.entityquery.issue.OrCriteria;
import io.onedev.server.entityquery.issue.StateCriteria;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.support.authorized.ProjectWriters;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.ValueIsEmpty;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.ValueIsNotEmpty;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.PressButtonTrigger;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.IssueChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.choiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;

public class IssueWorkflow implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private transient Map<String, StateSpec> stateSpecMap;
	
	private List<TransitionSpec> transitionSpecs = new ArrayList<>();
	
	private List<InputSpec> fieldSpecs = new ArrayList<>();
	
	private List<String> promptFieldsUponIssueOpen = new ArrayList<>();
	
	private boolean reconciled = true;
	
	private transient Map<String, InputSpec> fieldSpecMap;
	
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
		
		SpecifiedDefaultValue specifiedDefaultValue = new SpecifiedDefaultValue();
		specifiedDefaultValue.setValue("Bug");
		type.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(type);
		
		ChoiceInput priority = new ChoiceInput();
		priority.setName("Priority");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice low = new Choice();
		low.setValue("Low");
		low.setColor("#d9ead3");
		choices.add(low);

		Choice normal = new Choice();
		normal.setValue("Normal");
		normal.setColor("#f4cccc");
		choices.add(normal);

		Choice high = new Choice();
		high.setValue("High");
		high.setColor("#cc0000");
		choices.add(high);
		
		specifiedChoices.setChoices(choices);
		priority.setChoiceProvider(specifiedChoices);
		
		specifiedDefaultValue = new SpecifiedDefaultValue();
		specifiedDefaultValue.setValue("Normal");
		priority.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(priority);

		UserChoiceInput assignee = new UserChoiceInput();
		assignee.setChoiceProvider(new io.onedev.server.util.inputspec.userchoiceinput.choiceprovider.ProjectWriters());
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
		assigned.setColor("#9900ff");
		
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
		pressButton.setAuthorized(new ProjectWriters());
		pressButton.setPromptFields(Lists.newArrayList("Assignee"));
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open", "Assigned"));
		transition.setToState("Closed");
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Close");
		pressButton.setAuthorized(new ProjectWriters());
		pressButton.setPromptFields(Lists.newArrayList("Resolution", "Duplicate With"));
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		transition.setPrerequisite(new TransitionPrerequisite());
		transition.getPrerequisite().setInputName("Assignee");
		transition.getPrerequisite().setValueMatcher(new ValueIsEmpty());
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		transition.setRemoveFields(Lists.newArrayList("Resolution", "Duplicate With"));
		pressButton.setAuthorized(new ProjectWriters());
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Assigned");
		transition.setPrerequisite(new TransitionPrerequisite());
		transition.getPrerequisite().setInputName("Assignee");
		transition.getPrerequisite().setValueMatcher(new ValueIsNotEmpty());
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		transition.setRemoveFields(Lists.newArrayList("Resolution", "Duplicate With"));
		pressButton.setAuthorized(new ProjectWriters());
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		promptFieldsUponIssueOpen.add("Type");
		promptFieldsUponIssueOpen.add("Priority");
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

	public void setTransitionSpecs(List<TransitionSpec> stateTransitions) {
		this.transitionSpecs = stateTransitions;
	}

	public List<InputSpec> getFieldSpecs() {
		return fieldSpecs;
	}

	public void setFieldSpecs(List<InputSpec> fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
	}

	public List<String> getPromptFieldsUponIssueOpen() {
		return promptFieldsUponIssueOpen;
	}

	public void setPromptFieldsUponIssueOpen(List<String> promptFieldsUponIssueOpen) {
		this.promptFieldsUponIssueOpen = promptFieldsUponIssueOpen;
	}

	public boolean isReconciled() {
		return reconciled;
	}

	public void setReconciled(boolean reconciled) {
		this.reconciled = reconciled;
	}

	private Map<String, InputSpec> getFieldSpecMap() {
		if (fieldSpecMap == null) {
			fieldSpecMap = new LinkedHashMap<>();
			for (InputSpec field: getFieldSpecs())
				fieldSpecMap.put(field.getName(), field);
		}
		return fieldSpecMap;
	}
	
	private Map<String, StateSpec> getStateSpecMap() {
		if (stateSpecMap == null) {
			stateSpecMap = new LinkedHashMap<>();
			for (StateSpec state: getStateSpecs())
				stateSpecMap.put(state.getName(), state);
		}
		return stateSpecMap;
	}
	
	public List<String> getFieldNames() {
		return new ArrayList<>(getFieldSpecMap().keySet());
	}
	
	public void onDeleteConfiguration(String configurationName) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onDeleteConfiguration(configurationName))
				it.remove();
		}
	}
	
	public void onRenameConfiguration(String oldName, String newName) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) 
			it.next().onRenameConfiguration(oldName, newName);
	}
	
	public void onDeleteState(String stateName) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			transition.getFromStates().remove(stateName);
			if (transition.getFromStates().isEmpty() || transition.getToState().equals(stateName))
				it.remove();
		}
	}
	
	public void onRenameState(String oldName, String newName) {
		for (TransitionSpec transition: getTransitionSpecs()) {
			int index = transition.getFromStates().indexOf(oldName);
			if (index != -1)
				transition.getFromStates().set(index, newName);
			if (transition.getToState().equals(oldName))
				transition.setToState(newName);
		}
	}
	
	@Nullable
	public StateSpec getStateSpec(String stateName) {
		return getStateSpecMap().get(stateName);
	}

	public int getStateSpecIndex(String stateName) {
		for (int i=0; i<getStateSpecs().size(); i++) {
			if (getStateSpecs().get(i).getName().equals(stateName))
				return i;
		}
		return -1;
	}
	
	public int getTransitionSpecIndex(TransitionSpec transition) {
		for (int i=0; i<getTransitionSpecs().size(); i++) {
			if (getTransitionSpecs().get(i) == transition)
				return i;
		}
		return -1;
	}
	
	@Nullable
	public InputSpec getFieldSpec(String fieldName) {
		return getFieldSpecMap().get(fieldName);
	}

	public int getFieldSpecIndex(String fieldName) {
		for (int i=0; i<getFieldSpecs().size(); i++) {
			if (getFieldSpecs().get(i).getName().equals(fieldName))
				return i;
		}
		return -1;
	}
	
	public void onRenameField(String oldName, String newName) {
		for (int i=0; i<getPromptFieldsUponIssueOpen().size(); i++) {
			if (getPromptFieldsUponIssueOpen().get(i).equals(oldName))
				getPromptFieldsUponIssueOpen().set(i, newName);
		}
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onRenameField(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameInput(oldName, newName);
	}
	
	public void onDeleteField(String fieldName) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) { 
			if (it.next().onDeleteField(fieldName))
				it.remove();
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
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onRenameUser(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameUser(oldName, newName);
	}
	
	public void onDeleteUser(String userName) {
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
		
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onDeleteUser(userName))
				it.remove();
		}
	}
	
	public void onRenameGroup(String oldName, String newName) {
		for (TransitionSpec transition: getTransitionSpecs()) 
			transition.onRenameGroup(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameGroup(oldName, newName);
	}
	
	public void onDeleteGroup(String groupName) {
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
		
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			if (transition.onDeleteGroup(groupName))
				it.remove();
		}
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
	
}
