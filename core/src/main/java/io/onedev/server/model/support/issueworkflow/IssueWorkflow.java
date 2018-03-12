package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	public List<String> getScenarios() {
		List<String> scenarios = new ArrayList<>();
		for (StateSpec state: getStateSpecs())
			scenarios.add(state.getName());
		return scenarios;
	}

	@Override
	public Map<String, Input> getInputs() {
		Map<String, Input> inputs = new LinkedHashMap<>();
		for (Input field: getFieldSpecs())
			inputs.put(field.getName(), field);
		return inputs;
	}
	
}
