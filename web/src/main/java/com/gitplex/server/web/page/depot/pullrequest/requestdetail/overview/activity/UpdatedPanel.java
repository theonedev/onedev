package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.commitmessage.ExpandableCommitMessagePanel;
import com.gitplex.server.web.component.pullrequest.verificationstatus.VerificationStatusPanel;
import com.gitplex.server.web.page.depot.commit.CommitDetailPage;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.websocket.PullRequestChanged;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
class UpdatedPanel extends GenericPanel<PullRequestUpdate> {

	public UpdatedPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
			visitChildren(VerificationStatusPanel.class, new IVisitor<VerificationStatusPanel, Void>() {

				@Override
				public void component(VerificationStatusPanel object, IVisit<Void> visit) {
					pullRequestChanged.getPartialPageRequestHandler().add(object);
				}

			});
		}
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
				
				item.add(new AvatarLink("author", commit.getAuthorIdent(), new TooltipConfig()));

				IModel<Depot> depotModel = new AbstractReadOnlyModel<Depot>() {

					@Override
					public Depot getObject() {
						return getUpdate().getRequest().getTarget().getDepot();
					}
					
				};
				item.add(new ExpandableCommitMessagePanel("message", depotModel, item.getModel()));

				IModel<PullRequest> requestModel = new AbstractReadOnlyModel<PullRequest>() {

					@Override
					public PullRequest getObject() {
						return getUpdate().getRequest();
					}
					
				};
				item.add(new VerificationStatusPanel("verification", requestModel, Model.of(commit.name())));
				
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(depotModel.getObject(), commitState);
				Link<Void> hashLink = new BookmarkablePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

				DepotFilePage.State browseState = new DepotFilePage.State();
				browseState.blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
				params = DepotFilePage.paramsOf(depotModel.getObject(), browseState);
				item.add(new BookmarkablePageLink<Void>("browseCode", DepotFilePage.class, params));
				
				if (getUpdate().getRequest().getTarget().getObjectId(false) != null) {
					if (getUpdate().getRequest().getMergedCommits().contains(commit)) {
						item.add(AttributeAppender.append("class", " integrated"));
						item.add(AttributeAppender.append("title", "This commit has been integrated"));
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
