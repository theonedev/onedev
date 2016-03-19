package com.pmease.gitplex.web.component.commitlist;

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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.joda.time.DateTime;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.ContributorAvatars;
import com.pmease.gitplex.web.component.commitgraph.CommitGraphResourceReference;
import com.pmease.gitplex.web.component.commitgraph.CommitGraphUtils;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.contributionpanel.ContributionPanel;
import com.pmease.gitplex.web.component.hashandcode.HashAndCodePanel;
import com.pmease.gitplex.web.model.CommitRefsModel;

@SuppressWarnings("serial")
public class CommitListPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final IModel<List<Commit>> commitsModel;
	
	private final IModel<Map<String, List<String>>> labelsModel = new CommitRefsModel(new AbstractReadOnlyModel<Depot>() {

		@Override
		public Depot getObject() {
			return depotModel.getObject();
		}
		
	});
	
	private WebMarkupContainer container;
	
	public CommitListPanel(String id, IModel<Depot> depotModel, IModel<List<Commit>> commitsModel) {
		super(id);
		this.depotModel = depotModel;
		this.commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				List<Commit> commits = commitsModel.getObject();
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
		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<Commit>("commits", commitsModel) {

			private int itemIndex;
			
			@Override
			protected void onBeforeRender() {
				itemIndex = 0;
				super.onBeforeRender();
			}

			@Override
			protected void populateItem(ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				
				Fragment fragment;
				if (commit != null) {
					fragment = new Fragment("commit", "commitFrag", CommitListPanel.this);
					fragment.add(new ContributorAvatars("avatar", commit.getAuthor(), commit.getCommitter()));

					fragment.add(new CommitMessagePanel("message", depotModel, item.getModel()));

					RepeatingView labelsView = new RepeatingView("labels");

					List<String> commitLabels = labelsModel.getObject().get(commit.getHash());
					if (commitLabels == null)
						commitLabels = new ArrayList<>();
					for (String label: commitLabels) 
						labelsView.add(new Label(labelsView.newChildId(), label));
					fragment.add(labelsView);
					
					fragment.add(new ContributionPanel("contribution", commit.getAuthor(), commit.getCommitter()));
					fragment.add(new HashAndCodePanel("hashAndCode", depotModel, commit.getHash()));
					item.add(AttributeAppender.append("class", "commit clearfix commit-item-" + itemIndex++));
				} else {
					fragment = new Fragment("commit", "dateFrag", CommitListPanel.this);
					DateTime dateTime = new DateTime(getModelObject().get(item.getIndex()+1).getCommitter().getWhen());
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

	private List<Commit> separateByDate(List<Commit> commits) {
		List<Commit> separated = new ArrayList<>();
		DateTime groupTime = null;
		for (Commit commit: commits) {
			DateTime commitTime = new DateTime(commit.getCommitter().getWhen());
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
		
		response.render(JavaScriptHeaderItem.forReference(CommitGraphResourceReference.INSTANCE));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(CommitListPanel.class, "commit-list.css")));
		
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
