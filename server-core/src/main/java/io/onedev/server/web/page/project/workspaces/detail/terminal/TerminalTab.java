package io.onedev.server.web.page.project.workspaces.detail.terminal;

import org.apache.wicket.Page;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.model.Workspace;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceTab;
import io.onedev.server.workspace.WorkspaceService;

public class TerminalTab extends WorkspaceTab {

	private final String shellId;

	public TerminalTab(Workspace workspace, String shellId) {
		super(Model.of("Terminal " + (getTerminalIndex(workspace, shellId) + 1)), Model.of("terminal2"), WorkspaceTerminalPage.class, WorkspaceTerminalPage.paramsOf(workspace, shellId));
		this.shellId	= shellId;
	}

	private static int getTerminalIndex(Workspace workspace, String shellId) {
		return OneDev.getInstance(WorkspaceService.class).getShellIds(workspace).indexOf(shellId);
	}

	@Override
	public Component render(String componentId) {
		return new TerminalTabHead(componentId, this, shellId);
	}

	@Override
	public boolean isActive(Page currentPage) {
		if (super.isActive(currentPage)) {
			WorkspaceTerminalPage terminalPage = (WorkspaceTerminalPage) currentPage;
			return shellId.equals(terminalPage.getShellId());
		} else {
			return false;
		}
	}
}
