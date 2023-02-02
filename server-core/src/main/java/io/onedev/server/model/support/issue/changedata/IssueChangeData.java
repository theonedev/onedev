package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

public abstract class IssueChangeData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract String getActivity();

	public abstract Map<String, Collection<User>> getNewUsers();
	
	public abstract Map<String, Group> getNewGroups();
	
	public abstract boolean affectsListing();

	public boolean isMinor() {
		return false;
	}
	
	@Nullable
	public ActivityDetail getActivityDetail() {
		return null;
	}
	
}
