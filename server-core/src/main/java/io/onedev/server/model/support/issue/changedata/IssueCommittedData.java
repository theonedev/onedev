package io.onedev.server.model.support.issue.changedata;

import java.util.HashMap;
import java.util.List;
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

	private final List<String> commitHashes;
	
	public IssueCommittedData(List<String> commitHashes) {
		this.commitHashes = commitHashes;
	}
	
	public List<String> getCommitHashes() {
		return commitHashes;
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new IssueCommittedPanel(componentId, commitHashes) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected Issue getIssue() {
				return change.getIssue();
			}
		};
	}
	
	@Override
	public String getDescription() {
		return "New commits fixing this issue added";
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
