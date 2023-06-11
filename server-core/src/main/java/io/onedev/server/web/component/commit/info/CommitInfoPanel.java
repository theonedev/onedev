package io.onedev.server.web.component.commit.info;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.commit.status.CommitStatusLink;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.gitsignature.SignatureStatusPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

public abstract class CommitInfoPanel extends GenericPanel<ProjectScopedCommit> {
	
	public CommitInfoPanel(String id, IModel<ProjectScopedCommit> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = getCommit().getProject();
		String commitHash = getCommit().getCommitId().name();
		RevCommit revCommit = getCommit().getRevCommit();
		
		add(new ContributorAvatars("avatar", revCommit.getAuthorIdent(), revCommit.getCommitterIdent()));

		add(new CommitMessagePanel("message", new LoadableDetachableModel<RevCommit>() {

			@Override
			protected RevCommit load() {
				return getCommit().getRevCommit();
			}

		}) {

			@Override
			protected Project getProject() {
				return getCommit().getProject();
			}

		});

		add(new ContributorPanel("contribution", revCommit.getAuthorIdent(),
				revCommit.getCommitterIdent()));

		add(new BookmarkablePageLink<Void>("project", ProjectDashboardPage.class,
				ProjectDashboardPage.paramsOf(project)) {

			@Override
			public IModel<?> getBody() {
				return Model.of(getCommit().getProject().getPath());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getCommit().getProject().equals(getProject()));
			}

		});

		add(new SignatureStatusPanel("signature") {

			@Override
			protected RevObject getRevObject() {
				return getCommit().getRevCommit();
			}

		});

		add(new CommitStatusLink("buildStatus", getCommit().getCommitId(), null) {

			@Override
			protected Project getProject() {
				return getCommit().getProject();
			}

			@Override
			protected PullRequest getPullRequest() {
				return null;
			}

		});

		CommitDetailPage.State commitState = new CommitDetailPage.State();
		commitState.revision = commitHash;
		PageParameters params = CommitDetailPage.paramsOf(project, commitState);
		Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
		add(hashLink);
		hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commitHash)));
		add(new CopyToClipboardLink("copyHash", Model.of(commitHash)));

		BlobIdent blobIdent = new BlobIdent(commitHash, null, FileMode.TYPE_TREE);
		ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
		params = ProjectBlobPage.paramsOf(project, browseState);
		add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitInfoCssResourceReference()));
	}

	private ProjectScopedCommit getCommit() {
		return getModelObject();
	}
	
	protected abstract Project getProject();
	
}
