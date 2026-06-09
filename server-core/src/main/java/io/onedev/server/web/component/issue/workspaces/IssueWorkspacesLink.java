package io.onedev.server.web.component.issue.workspaces;

import org.apache.wicket.Component;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.workspace.speclist.WorkspaceSpecListPanel;

public abstract class IssueWorkspacesLink extends DropdownLink {

    public IssueWorkspacesLink(String id) {
        super(id);
    }

    @Override
    protected Component newContent(String id, FloatingPanel dropdown) {    
        var branch = getIssue().getBranch();
        if (branch != null) {
            return new WorkspaceSpecListPanel(id) {

                @Override
                protected Project getProject() {
                    return getIssue().getProject();
                }

                @Override
                protected String getBranch() {
                    return branch;
                }

                @Override
                protected ObjectId getCommitId() {
                    return getIssue().getProject().getObjectId(branch, true);
                }

            };
        } else {
            return new NoBranchWorkspacesPanel(id) {

                @Override
                protected Issue getIssue() {
                    return IssueWorkspacesLink.this.getIssue();
                }

            };
        }
    }

    protected abstract Issue getIssue();
    
    @Override
    protected void onConfigure() {
        super.onConfigure();

        if (getIssue().getProject().getHierarchyWorkspaceSpecs().isEmpty()) {
            setVisible(false);
        } else if (getIssue().getProject().canWriteCode(SecurityUtils.getSubject())) {
            setVisible(true);
        } else if (getIssue().getBranch() == null) {
            setVisible(false);
        } else {
            setVisible(getIssue().getProject().canCreateWorkspace(SecurityUtils.getSubject()));
        }
    }
}
