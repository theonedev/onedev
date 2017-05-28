package com.gitplex.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
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

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.commitmessage.ExpandableCommitMessagePanel;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;

@SuppressWarnings("serial")
class UpdatedPanel extends GenericPanel<PullRequestUpdate> {

	public UpdatedPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
				
				item.add(new AvatarLink("author", commit.getAuthorIdent(), true));

				IModel<Project> projectModel = new AbstractReadOnlyModel<Project>() {

					@Override
					public Project getObject() {
						return getUpdate().getRequest().getTarget().getProject();
					}
					
				};
				item.add(new ExpandableCommitMessagePanel("message", projectModel, item.getModel()));

				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

				BlobIdent blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
				ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
				browseState.requestId = getUpdate().getRequest().getId();
				params = ProjectBlobPage.paramsOf(projectModel.getObject(), browseState);
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
