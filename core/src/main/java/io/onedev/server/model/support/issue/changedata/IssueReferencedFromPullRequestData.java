package io.onedev.server.model.support.issue.changedata;

import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
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
	public String getDescription() {
		return "Referenced from pull request";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}
	
	@Override
	public Map<String, User> getNewUsers(Project project) {
		return null;
	}

	@Override
	public Map<String, Group> getNewGroups(Project project) {
		return null;
	}
	
}
