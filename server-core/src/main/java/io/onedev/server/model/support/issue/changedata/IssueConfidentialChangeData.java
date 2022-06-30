package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

public class IssueConfidentialChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final boolean oldConfidential;
	
	private final boolean newConfidential;
	
	public IssueConfidentialChangeData(boolean oldConfidential, boolean newConfidential) {
		this.oldConfidential = oldConfidential;
		this.newConfidential = newConfidential;
	}
	
	@Override
	public String getActivity() {
		return "changed confidential";
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
		return true;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Confidential", String.valueOf(oldConfidential));
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Confidential", String.valueOf(newConfidential));
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
