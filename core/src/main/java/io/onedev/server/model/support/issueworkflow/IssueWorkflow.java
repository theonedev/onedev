package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.InputContext;

public class IssueWorkflow implements Serializable, InputContext {
	
	private static final long serialVersionUID = 1L;
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private List<Input> fieldSpecs = new ArrayList<>();
	
	public List<StateSpec> getStateSpecs() {
		return stateSpecs;
	}

	public void setStateSpecs(List<StateSpec> stateSpecs) {
		this.stateSpecs = stateSpecs;
	}

	public List<Input> getFieldSpecs() {
		return fieldSpecs;
	}

	public void setFieldSpecs(List<Input> fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
	}

	@Override
	public List<String> getInputNames() {
		List<String> inputNames = new ArrayList<>();
		for (Input field: getFieldSpecs())
			inputNames.add(field.getName());
		return inputNames;
	}
	
	@Override
	public Input getInput(String inputName) {
		for (Input field: getFieldSpecs()) {
			if (field.getName().equals(inputName))
				return field;
		}
		throw new RuntimeException("Unable to find input with name: " + inputName);
	}
}
