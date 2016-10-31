package com.gitplex.web.component.commitlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

import com.gitplex.core.entity.Depot;
import com.gitplex.web.Constants;
import com.gitplex.web.component.avatar.ContributorAvatars;
import com.gitplex.web.component.commitgraph.CommitGraphResourceReference;
import com.gitplex.web.component.commitgraph.CommitGraphUtils;
import com.gitplex.web.component.commitmessage.ExpandableCommitMessagePanel;
import com.gitplex.web.component.contributorpanel.ContributorPanel;
import com.gitplex.web.model.CommitRefsModel;
import com.gitplex.web.page.depot.commit.CommitDetailPage;
import com.gitplex.web.page.depot.file.DepotFilePage;
import com.gitplex.commons.git.BlobIdent;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.wicket.behavior.clipboard.CopyClipboardBehavior;

@SuppressWarnings("serial")
public class CommitListPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final IModel<List<RevCommit>> commitsModel;
	
	private final IModel<Map<String, List<String>>> labelsModel = new CommitRefsModel(new AbstractReadOnlyModel<Depot>() {

		@Override
		public Depot getObject() {
			return depotModel.getObject();
		}
		
	});
	
	private WebMarkupContainer container;
	
	public CommitListPanel(String id, IModel<Depot> depotModel, IModel<List<RevCommit>> commitsModel) {
		super(id);
		this.depotModel = depotModel;
		this.commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = commitsModel.getObject();
				if (commits.size() > Constants.MAX_DISPLAY_COMMITS)
					commits = commits.subList(commits.size()-Constants.MAX_DISPLAY_COMMITS, commits.size());
				CommitGraphUtils.sort(commits, 0);
				return separateByDate(commits);
			}

			@Override
			protected void onDetach() {
				commitsModel.detach();
				super.onDetach();
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String tooManyMessage = "Too many commits, displaying recent " + Constants.MAX_DISPLAY_COMMITS;
		add(new Label("tooManyCommits", tooManyMessage) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commitsModel.getObject().size()>Constants.MAX_DISPLAY_COMMITS);
			}
			
		});
		add(new Label("noAnyCommits", "No any commits found") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commitsModel.getObject().isEmpty());
			}
			
		});
		
		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<RevCommit>("commits", commitsModel) {

			private int itemIndex;
			
			@Override
			protected void onBeforeRender() {
				itemIndex = 0;
				super.onBeforeRender();
			}

			@Override
			protected void populateItem(ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				
				Fragment fragment;
				if (commit != null) {
					fragment = new Fragment("commit", "commitFrag", CommitListPanel.this);
					fragment.add(new ContributorAvatars("avatar", 
							commit.getAuthorIdent(), commit.getCommitterIdent()));

					fragment.add(new ExpandableCommitMessagePanel("message", depotModel, item.getModel()));

					RepeatingView labelsView = new RepeatingView("labels");

					List<String> commitLabels = labelsModel.getObject().get(commit.name());
					if (commitLabels == null)
						commitLabels = new ArrayList<>();
					for (String label: commitLabels) 
						labelsView.add(new Label(labelsView.newChildId(), label));
					fragment.add(labelsView);
					
					fragment.add(new ContributorPanel("contribution", 
							commit.getAuthorIdent(), commit.getCommitterIdent(), true));

					CommitDetailPage.State commitState = new CommitDetailPage.State();
					commitState.revision = commit.name();
					PageParameters params = CommitDetailPage.paramsOf(depotModel.getObject(), commitState);
					Link<Void> hashLink = new BookmarkablePageLink<Void>("hashLink", CommitDetailPage.class, params);
					fragment.add(hashLink);
					hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
					fragment.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

					DepotFilePage.State browseState = new DepotFilePage.State();
					browseState.blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
					params = DepotFilePage.paramsOf(depotModel.getObject(), browseState);
					fragment.add(new BookmarkablePageLink<Void>("browseCode", DepotFilePage.class, params));
					
					item.add(AttributeAppender.append("class", "commit clearfix commit-item-" + itemIndex++));
				} else {
					fragment = new Fragment("commit", "dateFrag", CommitListPanel.this);
					DateTime dateTime = new DateTime(getModelObject().get(item.getIndex()+1).getCommitterIdent().getWhen());
					fragment.add(new Label("date", Constants.DATE_FORMATTER.print(dateTime)));
					if (item.getIndex() == 0)
						item.add(AttributeAppender.append("class", "date first"));
					else
						item.add(AttributeAppender.append("class", "date"));
				}				
				item.add(fragment);
			}
			
		});
		
	}

	private List<RevCommit> separateByDate(List<RevCommit> commits) {
		List<RevCommit> separated = new ArrayList<>();
		DateTime groupTime = null;
		for (RevCommit commit: commits) {
			DateTime commitTime = new DateTime(commit.getCommitterIdent().getWhen());
			if (groupTime == null || commitTime.getYear() != groupTime.getYear() 
					|| commitTime.getDayOfYear() != groupTime.getDayOfYear()) {
				groupTime = commitTime;
				separated.add(null);
			} 
			separated.add(commit);
		}
		return separated;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new CommitGraphResourceReference()));
		response.render(CssHeaderItem.forReference(new CommitListResourceReference()));
		
		String jsonOfCommits = CommitGraphUtils.asJSON(commitsModel.getObject());
		String script = String.format("gitplex.commitgraph.render('%s', %s);", container.getMarkupId(), jsonOfCommits);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		commitsModel.detach();
		
		super.onDetach();
	}

}
