package io.onedev.server.web.component.issue.commits;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Issue.FixCommit;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.commit.status.CommitStatusLink;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.gitsignature.GitSignaturePanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
public class IssueCommitsPanel extends GenericPanel<Issue> {

	public IssueCommitsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<FixCommit>("commits", new AbstractReadOnlyModel<List<FixCommit>>() {

			@Override
			public List<FixCommit> getObject() {
				return getIssue().getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<FixCommit> item) {
				FixCommit commit = item.getModelObject();
				Project project = commit.getProject();
				String commitHash = commit.getCommit().name();
				
				item.add(new ContributorAvatars("avatar", commit.getCommit().getAuthorIdent(), 
						commit.getCommit().getCommitterIdent()));

				item.add(new CommitMessagePanel("message", new LoadableDetachableModel<RevCommit>() {

					@Override
					protected RevCommit load() {
						return item.getModelObject().getCommit();
					}
					
				}) {

					@Override
					protected Project getProject() {
						return item.getModelObject().getProject();
					}
					
				});

				item.add(new ContributorPanel("contribution", commit.getCommit().getAuthorIdent(), 
						commit.getCommit().getCommitterIdent()));

				item.add(new BookmarkablePageLink<Void>("project", ProjectDashboardPage.class, 
						ProjectDashboardPage.paramsOf(project)) {

					@Override
					public IModel<?> getBody() {
						return Model.of(item.getModelObject().getProject().getPath());
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!item.getModelObject().getProject().equals(getIssue().getProject()));
					}
					
				});
				
				item.add(new GitSignaturePanel("signature") {

					@Override
					protected RevObject getRevObject() {
						return item.getModelObject().getCommit();
					}
					
				});
				
				item.add(new CommitStatusLink("buildStatus", commit.getCommit().copy(), null) {
					
					@Override
					protected Project getProject() {
						return item.getModelObject().getProject();
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
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commitHash)));
				item.add(new CopyToClipboardLink("copyHash", Model.of(commitHash)));

				BlobIdent blobIdent = new BlobIdent(commitHash, null, FileMode.TYPE_TREE);
				ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
				params = ProjectBlobPage.paramsOf(project, browseState);
				item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
				
				item.add(AttributeAppender.append("class", "commit"));				
			}
			
		});
		
	}

	private Issue getIssue() {
		return getModelObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueCommitsCssResourceReference()));
	}

}
