package io.onedev.server.model.support.issue.changedata;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.notification.ActivityDetail;

public class IssueStateChangeData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	public IssueStateChangeData(String oldState, String newState, 
			Map<String, Input> oldFields, Map<String, Input> newFields) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
	}

	@Override
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		oldFieldValues.put("State", oldState);
		oldFieldValues.putAll(super.getOldFieldValues());
		return oldFieldValues;
	}

	@Override
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		newFieldValues.put("State", newState);
		newFieldValues.putAll(super.getNewFieldValues());
		return newFieldValues;
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}

	@Override
	public String getActivity() {
		return "changed state to '" + newState + "'";
	}
	
	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.compare(getOldFieldValues(), getNewFieldValues(), true);
	}
	
}
