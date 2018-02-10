package com.turbodev.server.web.component.commitlist;

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

import com.turbodev.server.TurboDev;
import com.turbodev.server.git.BlobIdent;
import com.turbodev.server.git.GitUtils;
import com.turbodev.server.manager.VerificationManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.util.Verification;
import com.turbodev.server.web.WebConstants;
import com.turbodev.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.turbodev.server.web.component.avatar.ContributorAvatars;
import com.turbodev.server.web.component.commitgraph.CommitGraphResourceReference;
import com.turbodev.server.web.component.commitgraph.CommitGraphUtils;
import com.turbodev.server.web.component.commitmessage.ExpandableCommitMessagePanel;
import com.turbodev.server.web.component.contributorpanel.ContributorPanel;
import com.turbodev.server.web.component.link.ViewStateAwarePageLink;
import com.turbodev.server.web.component.verification.VerificationStatusPanel;
import com.turbodev.server.web.page.project.blob.ProjectBlobPage;
import com.turbodev.server.web.page.project.commit.CommitDetailPage;
import com.turbodev.server.web.util.model.CommitRefsModel;

@SuppressWarnings("serial")
public class CommitListPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<List<RevCommit>> commitsModel;
	
	private final IModel<Map<String, List<String>>> labelsModel = new CommitRefsModel(new AbstractReadOnlyModel<Project>() {

		@Override
		public Project getObject() {
			return projectModel.getObject();
		}
		
	});
	
	private WebMarkupContainer container;
	
	public CommitListPanel(String id, IModel<Project> projectModel, IModel<List<RevCommit>> commitsModel) {
		super(id);
		this.projectModel = projectModel;
		this.commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = commitsModel.getObject();
				if (commits.size() > WebConstants.MAX_DISPLAY_COMMITS)
					commits = commits.subList(commits.size()-WebConstants.MAX_DISPLAY_COMMITS, commits.size());
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
		
		String tooManyMessage = "Too many commits, displaying recent " + WebConstants.MAX_DISPLAY_COMMITS;
		add(new Label("tooManyCommits", tooManyMessage) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commitsModel.getObject().size()>WebConstants.MAX_DISPLAY_COMMITS);
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

					fragment.add(new ExpandableCommitMessagePanel("message", projectModel, item.getModel()));

					RepeatingView labelsView = new RepeatingView("labels");

					List<String> commitLabels = labelsModel.getObject().get(commit.name());
					if (commitLabels == null)
						commitLabels = new ArrayList<>();
					for (String label: commitLabels) 
						labelsView.add(new Label(labelsView.newChildId(), label));
					fragment.add(labelsView);
					
					fragment.add(new ContributorPanel("contribution", 
							commit.getAuthorIdent(), commit.getCommitterIdent(), true));

					String commitHash = commit.name();
					fragment.add(new VerificationStatusPanel("verificationStatus", 
							new LoadableDetachableModel<Map<String, Verification>>() {

						@Override
						protected Map<String, Verification> load() {
							return TurboDev.getInstance(VerificationManager.class)
									.getVerifications(projectModel.getObject(), commitHash);
						}
						
					}));
					
					CommitDetailPage.State commitState = new CommitDetailPage.State();
					commitState.revision = commit.name();
					PageParameters params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
					Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
					fragment.add(hashLink);
					hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
					fragment.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

					ProjectBlobPage.State browseState = new ProjectBlobPage.State(
							new BlobIdent(commit.name(), null, FileMode.TYPE_TREE));
					params = ProjectBlobPage.paramsOf(projectModel.getObject(), browseState);
					fragment.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
					
					item.add(AttributeAppender.append("class", "commit clearfix commit-item-" + itemIndex++));
				} else {
					fragment = new Fragment("commit", "dateFrag", CommitListPanel.this);
					DateTime dateTime = new DateTime(getModelObject().get(item.getIndex()+1).getCommitterIdent().getWhen());
					fragment.add(new Label("date", WebConstants.DATE_FORMATTER.print(dateTime)));
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
		String script = String.format("turbodev.server.commitgraph.render('%s', %s);", container.getMarkupId(), jsonOfCommits);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		commitsModel.detach();
		
		super.onDetach();
	}

}
