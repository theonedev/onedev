package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;

public class IssueTitleChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public IssueTitleChangeData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public String getActivity() {
		return "changed title";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
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
	public boolean affectsBoards() {
		return false;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Title", oldTitle);
		Map<String, String> newFieldValues = new HashMap<>();
		oldFieldValues.put("Title", newTitle);
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
