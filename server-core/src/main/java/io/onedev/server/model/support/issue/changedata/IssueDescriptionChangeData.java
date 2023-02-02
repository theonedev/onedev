package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IssueDescriptionChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public IssueDescriptionChangeData(@Nullable String oldDescription, @Nullable String newDescription) {
		if (oldDescription == null)
			oldDescription = "";
		this.oldDescription = oldDescription;
		if (newDescription == null)
			newDescription = "";
		this.newDescription = newDescription;
	}
	
	@Override
	public String getActivity() {
		return "changed description";
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

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Description", oldDescription);
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Description", newDescription);
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
