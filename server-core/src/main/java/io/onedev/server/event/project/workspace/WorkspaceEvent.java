package io.onedev.server.event.project.workspace;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.User;
import io.onedev.server.service.UrlService;
import io.onedev.server.workspace.WorkspaceService;

public abstract class WorkspaceEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;

	private final Long workspaceId;

	public WorkspaceEvent(User user, Date date, Workspace workspace) {
		super(user, date, workspace.getProject());
		workspaceId = workspace.getId();
	}

	public Workspace getWorkspace() {
		return OneDev.getInstance(WorkspaceService.class).load(workspaceId);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(getWorkspace(), true);
	}

}
