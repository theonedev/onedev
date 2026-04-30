package io.onedev.server.web.page.project.workspaces.detail.terminal;

import javax.inject.Inject;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.model.Workspace;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.terminal.TerminalPanel;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public class WorkspaceTerminalPage extends WorkspaceDetailPage {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_SHELL = "shell";

	private static final String PARAM_COMMAND = "command";

	private final String shellId;

	private final String command;

	@Inject
	private WorkspaceService workspaceService;

	public WorkspaceTerminalPage(PageParameters params) {
		super(params);
		shellId = params.get(PARAM_SHELL).toString();
		command = params.get(PARAM_COMMAND).toOptionalString();

		if (!workspaceService.getShellLabels(getWorkspace()).containsKey(shellId))
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
			protected String getInitialCommand() {
				return command;
			}

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
				workspaceService.onMessage(connection, getWorkspace(), shellId, "SHELL_INPUT:" + data);
			}

			@Override
			protected void onResized(IWebSocketConnection connection, int rows, int cols) {
				workspaceService.onMessage(connection, getWorkspace(), shellId,
						"TERMINAL_RESIZE:" + rows + "," + cols);
			}

			@Override
			protected boolean canWriteToStdin() {
				return SecurityUtils.canModifyOrDelete(getWorkspace());
			}

			@Override
			protected void onShellExit(IPartialPageRequestHandler handler) {
				setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(getWorkspace()));	
			}

		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		if (command != null) {
			var url = RequestCycle.get().urlFor(WorkspaceTerminalPage.class,
					WorkspaceTerminalPage.paramsOf(getWorkspace(), shellId)).toString();
			response.render(OnDomReadyHeaderItem.forScript(String.format(
					"onedev.server.history.replaceState('%s', undefined, '%s');",
					JavaScriptEscape.escapeJavaScript(url),
					JavaScriptEscape.escapeJavaScript(getPageTitle()))));
		}
	}

	public static PageParameters paramsOf(Workspace workspace, String shellId) {
		return paramsOf(workspace, shellId, null);
	}

	public static PageParameters paramsOf(Workspace workspace, String shellId, @Nullable String command) {
		var params = WorkspaceDetailPage.paramsOf(workspace);
		params.add(PARAM_SHELL, shellId);
		if (command != null)
			params.add(PARAM_COMMAND, command);
		return params;
	}

}
