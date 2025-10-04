package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IssueOwnSpentTimeChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final int oldValue;
	
	private final int newValue;
	
	public IssueOwnSpentTimeChangeData(int oldValue, int newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public String getActivity() {
		return "changed own spent time";
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

	@Override
	public boolean affectsListing() {
		return false;
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	public int getOldValue() {
		return oldValue;
	}

	public int getNewValue() {
		return newValue;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		var timeTrackingSetting = OneDev.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Own Spent Time", timeTrackingSetting.formatWorkingPeriod(oldValue, true));
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Own Spent Time", timeTrackingSetting.formatWorkingPeriod(newValue, true));
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
