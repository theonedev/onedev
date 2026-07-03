package io.onedev.server.web.component.issue.workspaces;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueService;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.workspace.speclist.WorkspaceSpecListPanel;

public abstract class IssueWorkspacesLink extends DropdownLink {

	@Inject
	private IssueService issueService;

	public IssueWorkspacesLink(String id) {
		super(id);
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new WorkspaceSpecListPanel(id) {

			private String branch;

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

			@Override
			protected String getBranch() {
				if (branch == null) {
					if (getIssue().getBranch() != null)
						branch = getIssue().getBranch();
					else
						branch = issueService.ensureBranch(SecurityUtils.getSubject(), getIssue());
				}
				return branch;
			}

			@Override
			protected ObjectId getCommitId() {
				return getIssue().getProject().getObjectId(getBranch(), true);
			}

			@Override
			protected Issue getIssue() {
				return IssueWorkspacesLink.this.getIssue();
			}

			@Override
			protected boolean isOnInfoVisible() {
				return false;
			}

		};
	}

	protected abstract Issue getIssue();
    
	@Override
	protected void onConfigure() {
		super.onConfigure();

		var issue = getIssue();
		setVisible(!issue.getProject().getHierarchyWorkspaceSpecs().isEmpty() 
				&& issue.getProject().canCreateWorkspace(SecurityUtils.getSubject()));
	}
}
