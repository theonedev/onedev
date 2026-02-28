package io.onedev.server.web.page.project.workspaces.detail.terminal;

import javax.inject.Inject;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Workspace;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.terminal.TerminalPanel;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public class WorkspaceTerminalPage extends WorkspaceDetailPage {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_SHELL = "shell";

	private final String shellId;

	@Inject
	private WorkspaceService workspaceService;

	public WorkspaceTerminalPage(PageParameters params) {
		super(params);
		shellId = params.get(PARAM_SHELL).toString();

		if (!workspaceService.getShellIds(getWorkspace()).contains(shellId))
			throw new RestartResponseException(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(getWorkspace()));
	}

	public String getShellId() {
		return shellId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new TerminalPanel("terminal") {

			@Override
			protected void onConnectionOpen(IWebSocketConnection connection) {
				workspaceService.onOpen(connection, getWorkspace(), shellId);
			}

			@Override
			protected void onConnectionClose(IWebSocketConnection connection) {
				workspaceService.onClose(connection);
			}

			@Override
			protected void writeToStdin(IWebSocketConnection connection, String data) {
				if (SecurityUtils.canModifyOrDelete(getWorkspace()))
					workspaceService.onMessage(connection, getWorkspace(), shellId, "SHELL_INPUT:" + data);
			}

			@Override
			protected void onResized(IWebSocketConnection connection, int rows, int cols) {
				workspaceService.onMessage(connection, getWorkspace(), shellId,
						"TERMINAL_RESIZE:" + rows + "," + cols);
			}

			@Override
			protected void onShellExit(IPartialPageRequestHandler handler) {
				setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(getWorkspace()));	
			}

		});
	}

	public static PageParameters paramsOf(Workspace workspace, String shellId) {
		var params = WorkspaceDetailPage.paramsOf(workspace);
		params.add(PARAM_SHELL, shellId);
		return params;
	}

}
