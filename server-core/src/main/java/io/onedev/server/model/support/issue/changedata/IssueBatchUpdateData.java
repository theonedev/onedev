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
	
	private final List<String> oldMilestones;
	
	private final List<String> newMilestones;
	
	public IssueBatchUpdateData(String oldState, String newState, 
			List<Milestone> oldMilestones, List<Milestone> newMilestones, 
			Map<String, Input> oldFields, Map<String, Input> newFields) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.oldMilestones = oldMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
		this.newMilestones = newMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		oldFieldValues.put("State", oldState);
		if (!oldMilestones.isEmpty())
			oldFieldValues.put("Milestones", StringUtils.join(oldMilestones));
		oldFieldValues.putAll(super.getOldFieldValues());
		return oldFieldValues;
	}

	@Override
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		newFieldValues.put("State", newState);
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
