package io.onedev.server.event.project.issue;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.web.UrlManager;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class IssueEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long issueId;
	
	public IssueEvent(User user, Date date, Issue issue) {
		super(user, date, issue.getProject());
		issueId = issue.getId();
	}

	public Issue getIssue() {
		return OneDev.getInstance(IssueManager.class).load(issueId);
	}
	
	public abstract boolean affectsListing();
	
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}
	
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}
	
	@Override
	public String getLockName() {
		return Issue.getSerialLockName(issueId);
	}
	
	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getIssue());
	}

}
