package io.onedev.server.model.support.issue.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.onedev.server.exception.OneException;
import io.onedev.server.model.support.authorized.ProjectWriters;
import io.onedev.server.model.support.issue.workflow.action.PressButton;
import io.onedev.server.util.UsageUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
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

	private Map<String, String> savedQueries = new LinkedHashMap<>();
	
	private List<String> listFields = new ArrayList<>();
	
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
		wontFix.setValue("Won't Fix");
		choices.add(wontFix);

		Choice duplicated = new Choice();
		duplicated.setValue("Duplicated");
		choices.add(duplicated);
		
		specifiedChoices.setChoices(choices);
		resolution.setChoiceProvider(specifiedChoices);
		
		fieldSpecs.add(resolution);

		IssueChoiceInput duplicateIssue = new IssueChoiceInput();
		duplicateIssue.setName("Duplicate With");
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("Resolution");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Duplicated"));
		showCondition.setValueMatcher(valueIsOneOf);
		duplicateIssue.setShowCondition(showCondition);
		fieldSpecs.add(duplicateIssue);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setColor("#f1c232");
		open.setFields(Lists.newArrayList("Type", "Priority", "Assignee"));
		
		stateSpecs.add(open);
		
		StateSpec closed = new StateSpec();
		closed.setColor("#cccccc");
		closed.setName("Closed");
		closed.setFields(Lists.newArrayList("Resolution", "Duplicate With"));
		
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
		
		listFields.add("Type");
		listFields.add("Priority");
		listFields.add("Assignee");
		
		savedQueries.put("All", "all");
		savedQueries.put("Outstanding", "\"State\" is not \"Closed\"");
		savedQueries.put("Closed", "\"State\" is \"Closed\"");
		savedQueries.put("Added recently", "\"Submit Date\" is after \"one week ago\"");
		savedQueries.put("Updated recently", "\"Update Date\" is after \"one week ago\"");
		savedQueries.put("Submitted by me", "\"Submitter\" is me");
		savedQueries.put("Assigned to me", "\"Assignee\" is me");
		savedQueries.put("Hight Priority", "\"Priority\" is \"High\"");
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

	public Map<String, String> getSavedQueries() {
		return savedQueries;
	}

	public void setSavedQueries(Map<String, String> savedQueries) {
		this.savedQueries = savedQueries;
	}

	public List<String> getListFields() {
		return listFields;
	}

	public void setListFields(List<String> listFields) {
		this.listFields = listFields;
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
	
	public List<String> onDeleteState(String stateName) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			TransitionSpec transition = it.next();
			transition.getFromStates().remove(stateName);
			if (transition.getFromStates().isEmpty() || transition.getToState().equals(stateName))
				it.remove();
		}
		return new ArrayList<>();
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
		for (StateSpec state: getStateSpecs()) 
			state.onFieldRename(oldName, newName);
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onFieldRename(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onInputRename(oldName, newName);
		int index = getListFields().indexOf(oldName);
		if (index != -1)
			getListFields().set(index, newName);
	}
	
	public List<String> onDeleteField(String fieldName) {
		List<String> usages = new ArrayList<>();
		for (StateSpec state: getStateSpecs())  
			usages.addAll(UsageUtils.prependCategory("Issue state '" + state.getName() + "'", state.onFieldDelete(fieldName)));
		for (TransitionSpec transition: getTransitionSpecs()) 
			usages.addAll(UsageUtils.prependCategory("Issue state transition '" + transition + "'", transition.onFieldDelete(fieldName)));
		for (InputSpec field: getFieldSpecs())
			usages.addAll(UsageUtils.prependCategory("Issue field '" + field.getName() + "'", field.onInputDelete(fieldName)));
		getListFields().remove(fieldName);
		return usages;
	}
	
	public void onRenameUser(String oldName, String newName) {
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onRenameUser(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameUser(oldName, newName);
	}
	
	public List<String> onDeleteUser(String userName) {
		List<String> usages = new ArrayList<>();
		for (InputSpec field: getFieldSpecs())
			usages.addAll(UsageUtils.prependCategory("Issue field '" + field.getName() + "'" , field.onDeleteUser(userName)));
		for (TransitionSpec transition: getTransitionSpecs())
			usages.addAll(UsageUtils.prependCategory("Issue state transition '" + transition + "'", transition.onDeleteUser(userName)));
		return usages;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		for (TransitionSpec transition: getTransitionSpecs()) 
			transition.onRenameGroup(oldName, newName);
		for (InputSpec field: getFieldSpecs())
			field.onRenameGroup(oldName, newName);
	}
	
	public List<String> onDeleteGroup(String groupName) {
		List<String> usages = new ArrayList<>();
		for (InputSpec field: getFieldSpecs()) 
			usages.addAll(UsageUtils.prependCategory("Issue field '"  + field.getName() + "'", field.onDeleteGroup(groupName)));
		for (TransitionSpec transition: getTransitionSpecs())
			usages.addAll(UsageUtils.prependCategory("Issue state transition '" + transition + "'", transition.onDeleteGroup(groupName)));
		return usages;
	}
	
	public StateSpec getInitialStateSpec() {
		if (!getStateSpecs().isEmpty())
			return getStateSpecs().iterator().next();
		else
			throw new OneException("No any issue state is defined");
	}
	
	public Collection<String> getApplicableFields(String state) {
		int index = getStateSpecIndex(state);
		Collection<String> applicableFields = new HashSet<>();
		for (int i=0; i<=index; i++)
			applicableFields.addAll(getStateSpecs().get(i).getFields());
		return applicableFields;
	}
	
}
