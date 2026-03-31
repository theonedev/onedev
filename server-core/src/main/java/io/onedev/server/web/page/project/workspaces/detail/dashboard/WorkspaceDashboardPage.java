package io.onedev.server.web.page.project.workspaces.detail.dashboard;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;
import io.onedev.server.web.page.project.workspaces.detail.log.WorkspaceLogPage;
import io.onedev.server.web.page.project.workspaces.detail.terminal.WorkspaceTerminalPage;

public class WorkspaceDashboardPage extends WorkspaceDetailPage {

	public WorkspaceDashboardPage(PageParameters params) {
		super(params);

		PageProvider pageProvider;

		var labels = workspaceService.getShellLabels(getWorkspace());
		if (!labels.isEmpty()) 
			pageProvider = new PageProvider(WorkspaceTerminalPage.class, WorkspaceTerminalPage.paramsOf(getWorkspace(), labels.keySet().iterator().next()));
		else
			pageProvider = new PageProvider(WorkspaceLogPage.class, WorkspaceLogPage.paramsOf(getWorkspace()));

		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

	public static PageParameters paramsOf(Workspace workspace) {
		return WorkspaceDetailPage.paramsOf(workspace);
	}

	public static PageParameters paramsOf(Project project, Long workspaceNumber) {
		return WorkspaceDetailPage.paramsOf(project, workspaceNumber);
	}

}
