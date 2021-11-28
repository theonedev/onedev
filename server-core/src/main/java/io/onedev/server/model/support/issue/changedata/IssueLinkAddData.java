package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentAware;

public class IssueLinkAddData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String linkName;
	
	private final String issueSummary;
	
	public IssueLinkAddData(String linkName, String issueSummary) {
		this.linkName = linkName;
		this.issueSummary = issueSummary;
	}
	
	@Override
	public String getActivity() {
		return "added '" + linkName + "': " + issueSummary;
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
