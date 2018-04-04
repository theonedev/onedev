package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.util.OneException;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;

public class IssueWorkflow implements Serializable, InputContext {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> states = new ArrayList<>();
	
	private transient Map<String, StateSpec> stateMap;
	
	private List<StateTransition> stateTransitions = new ArrayList<>();
	
	private List<InputSpec> fields = new ArrayList<>();
	
	private transient Map<String, InputSpec> fieldMap;
	
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
	
	public StateSpec getInitialState() {
		if (!getStates().isEmpty())
			return getStates().iterator().next();
		else
			throw new OneException("No any issue state is defined");
	}
	
}
