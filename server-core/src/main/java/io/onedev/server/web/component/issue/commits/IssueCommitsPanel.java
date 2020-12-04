package io.onedev.server.web.component.issue.commits;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
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

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

@SuppressWarnings("serial")
public class IssueCommitsPanel extends GenericPanel<Issue> {

	public IssueCommitsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<RevCommit>("commits", new AbstractReadOnlyModel<List<RevCommit>>() {

			@Override
			public List<RevCommit> getObject() {
				return getIssue().getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				
				item.add(new ContributorAvatars("avatar", commit.getAuthorIdent(), commit.getCommitterIdent()));

				item.add(new CommitMessagePanel("message", new LoadableDetachableModel<RevCommit>() {

					@Override
					protected RevCommit load() {
						return item.getModelObject();
					}
					
				}) {

					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}
					
				});

				item.add(new ContributorPanel("contribution", commit.getAuthorIdent(), commit.getCommitterIdent()));

				CommitStatusPanel commitStatus = new CommitStatusPanel("buildStatus", commit.copy(), null) {
					
					@Override
					protected String getCssClasses() {
						return "btn btn-outline-secondary";
					}

					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}

					@Override
					protected PullRequest getPullRequest() {
						return null;
					}
					
				};
				item.add(commitStatus);
				
				Project project = getIssue().getProject();
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(project, commitState);
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new CopyToClipboardLink("copyHash", Model.of(commit.name())));

				BlobIdent blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
				ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
				params = ProjectBlobPage.paramsOf(project, browseState);
				item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
				
				item.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						commitStatus.configure();
						if (commitStatus.isVisible())
							return "commit with-status";
						else
							return "commit";
					}
					
				}));				
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
