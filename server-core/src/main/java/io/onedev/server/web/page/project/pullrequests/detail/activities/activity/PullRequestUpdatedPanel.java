package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
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

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

@SuppressWarnings("serial")
class PullRequestUpdatedPanel extends GenericPanel<PullRequestUpdate> {

	public PullRequestUpdatedPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("age", DateUtils.formatAge(getUpdate().getDate())));
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
			protected void populateItem(final ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				
				item.add(new UserIdentPanel("author", UserIdent.of(commit.getAuthorIdent()), Mode.AVATAR));

				Project project = getUpdate().getRequest().getTarget().getProject();
				item.add(new CommitMessagePanel("message", project, item.getModel()));

				item.add(new CommitStatusPanel("buildStatus", project, commit.copy()) {
					
					@Override
					protected String getCssClasses() {
						return "btn btn-default btn-xs";
					}
					
				});
				
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(project, commitState);
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

				BlobIdent blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
				ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
				browseState.requestId = getUpdate().getRequest().getId();
				params = ProjectBlobPage.paramsOf(project, browseState);
				item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
				
				if (getUpdate().getRequest().getTarget().getObjectId(false) != null) {
					if (getUpdate().getRequest().getMergedCommits().contains(commit)) {
						item.add(AttributeAppender.append("class", " merged"));
						item.add(AttributeAppender.append("title", "This commit has been merged"));
					} else if (!getUpdate().getRequest().getPendingCommits().contains(commit)) {
						item.add(AttributeAppender.append("class", " rebased"));
						item.add(AttributeAppender.append("title", "This commit has been rebased"));
					}
				}
				
			}
			
		});
		
	}

	private PullRequestUpdate getUpdate() {
		return getModelObject();
	}
	
}
