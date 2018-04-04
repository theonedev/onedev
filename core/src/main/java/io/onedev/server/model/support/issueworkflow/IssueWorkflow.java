package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;

public class IssueWorkflow implements Serializable, InputContext {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> states = new ArrayList<>();
	
	private List<StateTransition> stateTransitions = new ArrayList<>();
	
	private List<InputSpec> fields = new ArrayList<>();
	
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

	@Override
	public List<String> getInputNames() {
		List<String> inputNames = new ArrayList<>();
		for (InputSpec field: getFields())
			inputNames.add(field.getName());
		return inputNames;
	}
	
	@Override
	public InputSpec getInput(String inputName) {
		for (InputSpec field: getFields()) {
			if (field.getName().equals(inputName))
				return field;
		}
		throw new RuntimeException("Unable to find input with name: " + inputName);
	}
	
	public void onStateDelete(String stateName) {
		for (Iterator<StateTransition> it = getStateTransitions().iterator(); it.hasNext();) {
			StateTransition transition = it.next();
			transition.getFromStates().remove(stateName);
			if (transition.getFromStates().isEmpty() || transition.getToState().equals(stateName))
				it.remove();
		}
	}
	
	public void onStateRename(String oldName, String newName) {
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
		for (StateSpec state: getStates()) {
			if (state.getName().equals(stateName))
				return state;
		}
		return null;
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
		for (InputSpec field: getFields()) {
			if (field.getName().equals(fieldName))
				return field;
		}
		return null;
	}

	public int getFieldIndex(String fieldName) {
		for (int i=0; i<getFields().size(); i++) {
			if (getFields().get(i).getName().equals(fieldName))
				return i;
		}
		return -1;
	}
	
	public void onFieldRename(String oldName, String newName) {
		for (StateSpec state: getStates()) 
			state.onFieldRename(oldName, newName);
		for (StateTransition transition: getStateTransitions()) {
			if (transition.getPrerequisite() != null && transition.getPrerequisite().getFieldName().equals(oldName))
				transition.getPrerequisite().setFieldName(newName);
		}
		for (InputSpec field: getFields())
			field.onInputRename(oldName, newName);
	}
	
	public void onFieldDelete(String fieldName) {
		for (StateSpec state: getStates()) 
			state.onFieldDelete(fieldName);
		for (InputSpec field: getFields())
			field.onInputDelete(fieldName);
		for (Iterator<StateTransition> it = getStateTransitions().iterator(); it.hasNext();) {
			StateTransition transition = it.next();
			if (transition.getPrerequisite() != null && transition.getPrerequisite().getFieldName().equals(fieldName))
				it.remove();
		}
	}
	
}
