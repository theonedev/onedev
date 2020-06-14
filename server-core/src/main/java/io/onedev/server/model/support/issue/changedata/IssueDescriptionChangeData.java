package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentAware;

public class IssueDescriptionChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public IssueDescriptionChangeData(String oldDescription, String newDescription) {
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}
	
	public String getOldDescription() {
		return oldDescription;
	}

	public String getNewDescription() {
		return newDescription;
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getActivity(Issue withIssue) {
		String activity = "changed description";
		if (withIssue != null)
			activity += " of issue " + withIssue.getNumberAndTitle();
		return activity;
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
	
}
