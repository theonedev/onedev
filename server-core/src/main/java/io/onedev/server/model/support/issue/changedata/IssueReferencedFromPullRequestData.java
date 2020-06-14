package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.pullrequest.referencedfrom.ReferencedFromPullRequestPanel;

public class IssueReferencedFromPullRequestData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public IssueReferencedFromPullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new ReferencedFromPullRequestPanel(componentId, requestId);
	}
	
	@Override
	public String getActivity(Issue withIssue) {
		if (withIssue != null)
			return "A pull request referenced issue " + withIssue.getNumberAndTitle();
		else
			return "Referenced from pull request";
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
