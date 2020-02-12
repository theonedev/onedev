package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.issue.activities.activity.IssueCommittedPanel;

public class IssueCommittedData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String commitHash;
	
	public IssueCommittedData(String commitHash) {
		this.commitHash = commitHash;
	}
	
	public String getCommitHash() {
		return commitHash;
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new IssueCommittedPanel(componentId, commitHash) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected Issue getIssue() {
				return change.getIssue();
			}
		};
	}
	
	@Override
	public String getActivity(Issue withIssue) {
		if (withIssue != null)
			return "Code committed to fix issue " + withIssue.describe();
		else
			return "Code committed to fix issue";
	}

	@Override
	public CommentSupport getCommentSupport() {
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
