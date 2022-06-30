package io.onedev.server.model.support.issue.changedata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.Input;

public class IssueBatchUpdateData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private final boolean oldConfidential;
	
	private final boolean newConfidential;
	
	private final List<String> oldMilestones;
	
	private final List<String> newMilestones;
	
	public IssueBatchUpdateData(String oldState, String newState, 
			boolean oldConfidential, boolean newConfidential,
			List<Milestone> oldMilestones, List<Milestone> newMilestones, 
			Map<String, Input> oldFields, Map<String, Input> newFields) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.oldConfidential = oldConfidential;
		this.newConfidential = newConfidential;
		this.oldMilestones = oldMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
		this.newMilestones = newMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		oldFieldValues.put("State", oldState);
		oldFieldValues.put("Confidential", String.valueOf(oldConfidential));
		if (!oldMilestones.isEmpty())
			oldFieldValues.put("Milestones", StringUtils.join(oldMilestones));
		oldFieldValues.putAll(super.getOldFieldValues());
		return oldFieldValues;
	}

	@Override
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		newFieldValues.put("State", newState);
		newFieldValues.put("Confidential", String.valueOf(newConfidential));
		if (!newMilestones.isEmpty())
			newFieldValues.put("Milestones", StringUtils.join(newMilestones));
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

	public List<String> getOldMilestones() {
		return oldMilestones;
	}

	public List<String> getNewMilestones() {
		return newMilestones;
	}

	@Override
	public String getActivity() {
		return "batch edited";
	}
	
}
