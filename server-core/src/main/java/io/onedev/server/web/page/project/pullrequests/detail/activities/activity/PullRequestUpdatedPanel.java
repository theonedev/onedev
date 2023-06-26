package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import com.google.common.collect.Sets;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.gitsignature.SignatureStatusPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.PersonIdentPanel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
class PullRequestUpdatedPanel extends GenericPanel<PullRequestUpdate> {

	public PullRequestUpdatedPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("age", DateUtils.formatAge(getUpdate().getDate()))
				.add(new AttributeAppender("title", DateUtils.formatDateTime(getUpdate().getDate()))));
		String tooManyMessage = "Too many commits, displaying recent " + WebConstants.MAX_DISPLAY_COMMITS;
		add(new Label("tooManyCommits", tooManyMessage) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUpdate().getCommits().size()>WebConstants.MAX_DISPLAY_COMMITS);
			}
			
		});
		add(new ListView<RevCommit>("commits", new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = getUpdate().getCommits();
				if (commits.size() > WebConstants.MAX_DISPLAY_COMMITS)
					return commits.subList(commits.size()-WebConstants.MAX_DISPLAY_COMMITS, commits.size());
				else 
					return commits;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				if (!getUpdate().getRequest().getPendingCommits().contains(commit)) {
					item.add(AttributeAppender.append("class", "rebased"));
					item.add(AttributeAppender.append("title", "This commit is rebased"));
				}
				
				item.add(new PersonIdentPanel("author", commit.getAuthorIdent(), "Author", Mode.AVATAR));

				item.add(new CommitMessagePanel("message", item.getModel()) {

					@Override
					protected Project getProject() {
						return getUpdate().getRequest().getTarget().getProject(); 
					}
					
				});

				item.add(new SignatureStatusPanel("signature") {

					@Override
					protected RevObject getRevObject() {
						return item.getModelObject();
					}
					
				});
				
				Project project = getUpdate().getRequest().getTarget().getProject();
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(project, commitState);
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new CopyToClipboardLink("copyHash", Model.of(commit.name())));

				BlobIdent blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
				ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
				browseState.requestId = getUpdate().getRequest().getId();
				params = ProjectBlobPage.paramsOf(project, browseState);
				item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
			}
			
		});
		
		add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(PullRequest.getChangeObservable(getUpdate().getRequest().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	private PullRequestUpdate getUpdate() {
		return getModelObject();
	}
	
}
