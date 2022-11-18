package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

public class IssueProjectChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldProject;
	
	private final String newProject;
	
	public IssueProjectChangeData(String oldProject, String newProject) {
		this.oldProject = oldProject;
		this.newProject = newProject;
	}
	
	@Override
	public String getActivity() {
		return "moved";
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
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Project", oldProject);
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Project", newProject);
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
