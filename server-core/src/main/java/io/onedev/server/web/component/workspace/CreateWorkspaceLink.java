package io.onedev.server.web.component.workspace;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public abstract class CreateWorkspaceLink extends AjaxLink<Void> {

	@Inject
	private WorkspaceService workspaceService;

	private final String branch;

	public CreateWorkspaceLink(String componentId, String branch) {
		super(componentId);
		this.branch = branch;
	}

	protected abstract Project getProject();

	protected abstract String getSpecName();

	@Override
	public void onClick(AjaxRequestTarget target) {
		var workspace = new Workspace();
		workspace.setProject(getProject());
		workspace.setUser(SecurityUtils.getUser());
		workspace.setBranch(branch);
		workspace.setSpecName(getSpecName());
		workspace.setToken(UUID.randomUUID().toString());
		workspaceService.create(workspace);
		setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(workspace));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canWriteCode(getProject()));
	}

}
