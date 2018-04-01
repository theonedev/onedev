package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
			transition.getToStates().remove(stateName);
			if (transition.getFromStates().isEmpty() || transition.getToStates().isEmpty())
				it.remove();
		}
	}
	
	public void onStateRename(String oldName, String newName) {
		for (StateTransition transition: getStateTransitions()) {
			int index = transition.getFromStates().indexOf(oldName);
			if (index != -1)
				transition.getFromStates().set(index, newName);
			index = transition.getToStates().indexOf(oldName);
			if (index != -1)
				transition.getToStates().set(index, newName);
		}
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
