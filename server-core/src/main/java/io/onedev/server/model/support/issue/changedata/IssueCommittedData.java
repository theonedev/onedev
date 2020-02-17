package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.issue.activities.activity.IssueCommittedPanel;

public class IssueCommittedData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String commitHash;
	
	private transient Optional<User> committer;
	
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
			return "Code committed for issue " + withIssue.describe();
		else
			return "Code committed";
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

	@Nullable
	public User getCommitter(Issue issue) {
		if (committer == null) {
			RevCommit commit = issue.getProject().getRevCommit(getCommitHash(), false);
			if (commit != null) 
				committer = Optional.ofNullable(OneDev.getInstance(UserManager.class).find(commit.getCommitterIdent()));
			else
				committer = Optional.empty();
		}
		return committer.orElse(null);
	}
	
	@Override
	public boolean affectsBoards() {
		return false;
	}
	
}
