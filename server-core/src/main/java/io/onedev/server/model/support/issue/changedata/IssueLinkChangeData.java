package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;

public class IssueLinkChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String linkName;
	
	private final String oldIssueSummary;
	
	private final String newIssueSummary;
	
	public IssueLinkChangeData(String linkName, @Nullable String oldIssueSummary, @Nullable String newIssueSummary) {
		this.linkName = linkName;
		this.oldIssueSummary = oldIssueSummary;
		this.newIssueSummary = newIssueSummary;
	}
	
	@Override
	public String getActivity() {
		return "changed \"" + linkName + "\"";
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
		oldFieldValues.put(linkName, oldIssueSummary);
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put(linkName, newIssueSummary);
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
