package com.pmease.gitplex.web.component.commitlist;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.joda.time.DateTime;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.ContributorAvatars;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.contributionpanel.ContributionPanel;
import com.pmease.gitplex.web.component.hashandcode.HashAndCodePanel;

@SuppressWarnings("serial")
public class CommitListPanel extends Panel {

	private final IModel<Depot> repoModel;
	
	private final IModel<List<Commit>> commitsModel;
	
	public CommitListPanel(String id, IModel<Depot> repoModel, IModel<List<Commit>> commitsModel) {
		super(id);
		this.repoModel = repoModel;
		this.commitsModel = commitsModel;
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
		add(new ListView<Commit>("commits", new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				List<Commit> commits = commitsModel.getObject();
				if (commits.size() > Constants.MAX_DISPLAY_COMMITS)
					commits = commits.subList(commits.size()-Constants.MAX_DISPLAY_COMMITS, commits.size());
				return separateByDate(commits);
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				
				if (commit != null) {
					Fragment fragment = new Fragment("commit", "commitFrag", CommitListPanel.this);
					fragment.add(new ContributorAvatars("avatar", commit.getAuthor(), commit.getCommitter()));
					fragment.add(new CommitMessagePanel("message", repoModel, item.getModel()));
					fragment.add(new ContributionPanel("contribution", commit.getAuthor(), commit.getCommitter()));
					fragment.add(new HashAndCodePanel("hashAndCode", repoModel, commit.getHash()));
					fragment.add(AttributeAppender.append("class", "commit clearfix"));
					
					item.add(fragment);
				} else {
					Fragment fragment = new Fragment("commit", "dateFrag", CommitListPanel.this);
					DateTime dateTime = new DateTime(getModelObject().get(item.getIndex()+1).getCommitter().getWhen());
					fragment.add(new Label("date", Constants.DATE_FORMATTER.print(dateTime)));
					if (item.getIndex() == 0)
						fragment.add(AttributeAppender.append("class", "date first"));
					else
						fragment.add(AttributeAppender.append("class", "date"));
					item.add(fragment);
				}				
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
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(CommitListPanel.class, "commit-list.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitsModel.detach();
		
		super.onDetach();
	}

}
