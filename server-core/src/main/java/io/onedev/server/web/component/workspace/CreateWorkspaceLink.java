package io.onedev.server.web.component.workspace;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public abstract class CreateWorkspaceLink extends AjaxLink<Void> {

	@Inject
	private WorkspaceService workspaceService;

	public CreateWorkspaceLink(String componentId) {
		super(componentId);
	}

	protected abstract Project getProject();

	protected abstract String getBranch();

	protected abstract WorkspaceSpec getSpec();

	@Override
	public void onClick(AjaxRequestTarget target) {
		var workspace = workspaceService.create(SecurityUtils.getUser(), getProject(), getBranch(), getSpec().getName());
		setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(workspace));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canWriteCode(getProject()));
	}

}
