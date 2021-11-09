package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentAware;

public class IssueMilestoneRemoveData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String milestone;
	
	public IssueMilestoneRemoveData(String milestone) {
		this.milestone = milestone;
	}
	
	@Override
	public String getActivity() {
		return "removed from milestone \"" + milestone + "\"";
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

}
