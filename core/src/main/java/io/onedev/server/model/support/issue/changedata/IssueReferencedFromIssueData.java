package io.onedev.server.model.support.issue.changedata;

import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.issue.referencedfrom.ReferencedFromIssuePanel;

public class IssueReferencedFromIssueData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final Long issueId;
	
	public IssueReferencedFromIssueData(Issue issue) {
		this.issueId = issue.getId();
	}
	
	public Long getIssueId() {
		return issueId;
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new ReferencedFromIssuePanel(componentId, issueId);
	}
	
	@Override
	public String getDescription() {
		return "Referenced from other issue";
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

	@Override
	public boolean affectsBoards() {
		return false;
	}
	
}
