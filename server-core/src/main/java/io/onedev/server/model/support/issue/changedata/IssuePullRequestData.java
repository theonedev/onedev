package io.onedev.server.model.support.issue.changedata;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.issue.activities.activity.IssuePullRequestPanel;

public abstract class IssuePullRequestData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public IssuePullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	@Nullable
	public PullRequest getPullRequest() {
		return OneDev.getInstance(PullRequestManager.class).get(requestId);
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new IssuePullRequestPanel(componentId, requestId);
	}
	
	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}
	
	@Override
	public Map<String, User> getNewUsers() {
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
