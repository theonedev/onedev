package io.onedev.server.model.support.issue.changedata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Iteration;
import io.onedev.server.buildspecmodel.inputspec.Input;

public class IssueBatchUpdateData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private final boolean oldConfidential;
	
	private final boolean newConfidential;
	
	private final List<String> oldIterations;
	
	private final List<String> newIterations;
	
	public IssueBatchUpdateData(String oldState, String newState,
								boolean oldConfidential, boolean newConfidential,
								List<Iteration> oldIterations, List<Iteration> newIterations,
								Map<String, Input> oldFields, Map<String, Input> newFields) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.oldConfidential = oldConfidential;
		this.newConfidential = newConfidential;
		this.oldIterations = oldIterations.stream().map(it->it.getName()).collect(Collectors.toList());
		this.newIterations = newIterations.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		oldFieldValues.put("State", oldState);
		oldFieldValues.put("Confidential", String.valueOf(oldConfidential));
		if (!oldIterations.isEmpty())
			oldFieldValues.put("Iterations", StringUtils.join(oldIterations));
		oldFieldValues.putAll(super.getOldFieldValues());
		return oldFieldValues;
	}

	@Override
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		newFieldValues.put("State", newState);
		newFieldValues.put("Confidential", String.valueOf(newConfidential));
		if (!newIterations.isEmpty())
			newFieldValues.put("Iterations", StringUtils.join(newIterations));
		newFieldValues.putAll(super.getNewFieldValues());
		return newFieldValues;
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}

	public boolean isOldConfidential() {
		return oldConfidential;
	}

	public boolean isNewConfidential() {
		return newConfidential;
	}

	public List<String> getOldIterations() {
		return oldIterations;
	}

	public List<String> getNewIterations() {
		return newIterations;
	}

	@Override
	public String getActivity() {
		return "batch edited";
	}
	
}
