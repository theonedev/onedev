package io.onedev.server.event.project.issue;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class IssueChanged extends IssueEvent implements CommitAware {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String comment;
	
	public IssueChanged(IssueChange change, @Nullable String comment) {
		super(change.getUser(), change.getDate(), change.getIssue());
		changeId = change.getId();
		this.comment = comment;
	}

	public IssueChange getChange() {
		return OneDev.getInstance(IssueChangeManager.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return comment!=null? new MarkdownText(getProject(), comment): null;
	}

	@Nullable
	public String getComment() {
		return comment;
	}

	@Override
	public boolean affectsListing() {
		return getChange().affectsBoards();
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return getChange().getData().getNewUsers();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return getChange().getData().getNewGroups();
	}

	@Override
	public String getActivity() {
		return getChange().getData().getActivity();
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return getChange().getData().getActivityDetail();
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getChange());
	}

	@Override
	public boolean isMinor() {
		return getChange().isMinor();
	}

	@Override
	public ProjectScopedCommit getCommit() {
		if (getChange().getData() instanceof IssueStateChangeData) {
			var project = getIssue().getProject();
			if (project.getDefaultBranch() != null)
				return new ProjectScopedCommit(project, project.getObjectId(project.getDefaultBranch(), true));
			else
				return null;
		} else {
			return null;
		}
	}

}
