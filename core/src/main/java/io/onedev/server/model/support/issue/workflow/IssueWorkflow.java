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

import io.onedev.server.exception.OneException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.authorized.ProjectWriters;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.OrCriteria;
import io.onedev.server.model.support.issue.query.StateCriteria;
import io.onedev.server.model.support.issue.workflow.action.PressButton;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.util.inputspec.choiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.util.inputspec.issuechoiceinput.IssueChoiceInput;
import io.onedev.server.util.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.util.inputspec.userchoiceinput.choiceprovider.ProjectReaders;

public class IssueWorkflow implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private transient Map<String, StateSpec> stateSpecMap;
	
	private List<TransitionSpec> transitionSpecs = new ArrayList<>();
	
	private List<InputSpec> fieldSpecs = new ArrayList<>();

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
		assignee.setAllowEmpty(true);
		assignee.setNameOfEmptyValue("No assignee");
		assignee.setChoiceProvider(new ProjectReaders());
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

		List<ShowCondition> showConditions = new ArrayList<>();
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("State");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Closed"));
		showCondition.setValueMatcher(valueIsOneOf);
		showConditions.add(showCondition);
		resolution.setShowConditions(showConditions);
		
		fieldSpecs.add(resolution);

		IssueChoiceInput duplicateIssue = new IssueChoiceInput();
		duplicateIssue.setName("Duplicate With");
		
		showConditions = new ArrayList<>();
		showCondition = new ShowCondition();
		showCondition.setInputName("State");
		valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Closed"));
		showCondition.setValueMatcher(valueIsOneOf);
		showConditions.add(showCondition);
		
		showCondition = new ShowCondition();
		showCondition.setInputName("Resolution");
		valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Duplicated"));
		showCondition.setValueMatcher(valueIsOneOf);
		showConditions.add(showCondition);
		
		duplicateIssue.setShowConditions(showConditions);
		
		fieldSpecs.add(duplicateIssue);
		
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
		PressButton pressButton = new PressButton();
		pressButton.setName("Close");
		pressButton.setAuthorized(new ProjectWriters());
		transition.setOnAction(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		pressButton = new PressButton();
		pressButton.setName("Reopen");
		pressButton.setAuthorized(new ProjectWriters());
		transition.setOnAction(pressButton);
		
		transitionSpecs.add(transition);
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
		if (fieldName.equals(Issue.FIELD_STATE))
			return getFieldSpecOfState();
		else
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
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onFieldRename(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameInput(oldName, newName);
	}
	
	public void onDeleteField(String fieldName) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) { 
			if (it.next().onFieldDelete(fieldName))
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
	
	public InputSpec getFieldSpecOfState() {
		ChoiceInput inputSpec = new ChoiceInput();
		inputSpec.setName(Issue.FIELD_STATE);
		inputSpec.setAllowEmpty(false);
		SpecifiedChoices choicesProvider = new SpecifiedChoices();
		for (StateSpec stateSpec: getStateSpecs()) {
			Choice choice = new Choice();
			choice.setValue(stateSpec.getName());
			choice.setColor(stateSpec.getColor());
			choicesProvider.getChoices().add(choice);
		}
		inputSpec.setChoiceProvider(choicesProvider);
		return inputSpec;
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
