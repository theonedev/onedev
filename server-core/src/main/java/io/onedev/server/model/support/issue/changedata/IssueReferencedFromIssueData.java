package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.rest.annotation.EntityId;
import io.onedev.server.util.CommentAware;

public class IssueReferencedFromIssueData extends IssueChangeData implements ReferencedFromAware<Issue> {

	private static final long serialVersionUID = 1L;

	@EntityId(Issue.class)
	private final Long issueId;
	
	public IssueReferencedFromIssueData(Issue issue) {
		this.issueId = issue.getId();
	}
	
	public Long getIssueId() {
		return issueId;
	}

	@Override
	public String getActivity() {
		return "Referenced from other issue";
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
	public Issue getReferencedFrom() {
		return OneDev.getInstance(IssueManager.class).get(issueId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}

}
