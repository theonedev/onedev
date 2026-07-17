package io.onedev.server.web.component.workspace;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDefaultPage;
import io.onedev.server.workspace.WorkspaceService;

public abstract class CreateWorkspaceLink extends AjaxLink<Void> {

	@Inject
	private WorkspaceService workspaceService;

	public CreateWorkspaceLink(String componentId) {
		super(componentId);
	}

	protected abstract Project getProject();

	@Nullable
	protected abstract String getBranch();

	protected abstract ObjectId getCommitId();

	protected abstract WorkspaceSpec getSpec();

	@Nullable
	protected Issue getIssue() {
		return null;
	}

	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		var workspace = workspaceService.create(SecurityUtils.getUser(), getProject(), getIssue(),
				getPullRequest(), getCommitId(), getBranch(), getSpec().getName(), false);
		setResponsePage(WorkspaceDefaultPage.class, WorkspaceDefaultPage.paramsOf(workspace));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getProject().canCreateWorkspace(SecurityUtils.getSubject()));
	}

}
