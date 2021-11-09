package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Group;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;

public class IssueMilestoneChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final List<String> oldMilestones;
	
	private final List<String> newMilestones;
	
	public IssueMilestoneChangeData(List<Milestone> oldMilestones, List<Milestone> newMilestones) {
		this.oldMilestones = oldMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
		this.newMilestones = newMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	public List<String> getOldMilestones() {
		return oldMilestones;
	}

	public List<String> getNewMilestones() {
		return newMilestones;
	}

	@Override
	public String getActivity() {
		return "changed milestones";
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
	public boolean affectsListing() {
		return true;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Milestones", StringUtils.join(oldMilestones));
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Milestones", StringUtils.join(newMilestones));
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
