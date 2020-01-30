package io.onedev.server.event.issue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public abstract class IssueEvent extends ProjectEvent {

	private final Issue issue;
	
	public IssueEvent(User user, Date date, Issue issue) {
		super(user, date, issue.getProject());
		this.issue = issue;
	}

	public Issue getIssue() {
		return issue;
	}
	
	public abstract boolean affectsBoards();
	
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}
	
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}
	
}
