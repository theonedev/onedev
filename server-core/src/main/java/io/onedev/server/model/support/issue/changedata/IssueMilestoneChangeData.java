package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Group;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;

public class IssueMilestoneChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldMilestone;
	
	private final String newMilestone;
	
	public IssueMilestoneChangeData(@Nullable Milestone oldMilestone, @Nullable Milestone newMilestone) {
		this.oldMilestone = oldMilestone!=null?oldMilestone.getName():null;
		this.newMilestone = newMilestone!=null?newMilestone.getName():null;
	}
	
	public String getOldMilestone() {
		return oldMilestone;
	}

	public String getNewMilestone() {
		return newMilestone;
	}

	@Override
	public String getActivity() {
		return "changed milestone";
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
		return true;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Milestone", oldMilestone);
		Map<String, String> newFieldValues = new HashMap<>();
		oldFieldValues.put("Milestone", newMilestone);
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
