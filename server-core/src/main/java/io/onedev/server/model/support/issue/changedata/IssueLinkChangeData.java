package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class IssueLinkChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String linkName;
	
	private final boolean opposite;
	
	private final String linkedIssueNumber;
	
	public IssueLinkChangeData(String linkName, boolean opposite, String linkedIssueNumber) {
		this.linkName = linkName;
		this.opposite = opposite;
		this.linkedIssueNumber = linkedIssueNumber;
	}

	public String getLinkName() {
		return linkName;
	}

	public boolean isOpposite() {
		return opposite;
	}

	public String getLinkedIssueNumber() {
		return linkedIssueNumber;
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
