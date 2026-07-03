package io.onedev.server.web.component.workspace.on;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.commit.CommitLink;
import io.onedev.server.web.page.project.issues.detail.IssueDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;

public class WorkspaceOnPanel extends GenericPanel<Workspace> {

	public WorkspaceOnPanel(String id, IModel<Workspace> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var workspace = getModelObject();
		if (workspace.getIssue() != null) {
			add(new BookmarkablePageLink<Void>("link", IssueDetailPage.class,
					IssueDetailPage.paramsOf(workspace.getIssue()))
					.setBody(Model.of(workspace.getOnDescription())));
		} else if (workspace.getRequest() != null) {
			add(new BookmarkablePageLink<Void>("link", PullRequestDetailPage.class,
					PullRequestDetailPage.paramsOf(workspace.getRequest()))
					.setBody(Model.of(workspace.getOnDescription())));
		} else if (workspace.getBranch() != null) {
			add(new BranchLink("link", new ProjectAndBranch(workspace.getProject(), workspace.getBranch())) {

				@Override
				public IModel<?> getBody() {
					return Model.of(workspace.getOnDescription());
				}

			});
		} else {
			add(new CommitLink("link", workspace.getProject(), ObjectId.fromString(workspace.getCommitHash())) {

				@Override
				public IModel<?> getBody() {
					return Model.of(workspace.getOnDescription());
				}

			});
		}
	}

}
