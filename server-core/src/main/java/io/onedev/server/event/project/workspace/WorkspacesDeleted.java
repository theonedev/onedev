package io.onedev.server.event.project.workspace;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.security.SecurityUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

public class WorkspacesDeleted extends ProjectEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Collection<Long> workspaceIds = new HashSet<>();

	private final Collection<Long> issueIds = new HashSet<>();

	private final Collection<Long> pullRequestIds = new HashSet<>();

	public WorkspacesDeleted(Project project, Collection<Workspace> workspaces) {
		super(SecurityUtils.getUser(), new Date(), project);
		for (var workspace: workspaces) {
			workspaceIds.add(workspace.getId());
			if (workspace.getIssue() != null)
				issueIds.add(workspace.getIssue().getId());
			if (workspace.getRequest() != null)
				pullRequestIds.add(workspace.getRequest().getId());
		}
	}

	public Collection<Long> getWorkspaceIds() {
		return workspaceIds;
	}

	public Collection<Long> getIssueIds() {
		return issueIds;
	}

	public Collection<Long> getPullRequestIds() {
		return pullRequestIds;
	}

	@Override
	public String getActivity() {
		return "workspaces deleted";
	}

}
