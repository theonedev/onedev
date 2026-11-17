package io.onedev.server.event.project.workspace;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.security.SecurityUtils;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Date;

public class WorkspaceDeleted extends ProjectEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Long workspaceId;

	private final Long issueId;

	private final Long pullRequestId;

	public WorkspaceDeleted(Workspace workspace) {
		super(SecurityUtils.getUser(), new Date(), workspace.getProject());
		workspaceId = workspace.getId();
		issueId = Issue.idOf(workspace.getIssue());
		pullRequestId = PullRequest.idOf(workspace.getRequest());
	}

	public Long getWorkspaceId() {
		return workspaceId;
	}

	@Nullable
	public Long getIssueId() {
		return issueId;
	}

	@Nullable
	public Long getPullRequestId() {
		return pullRequestId;
	}

	@Override
	public String getActivity() {
		return "deleted";
	}

}
