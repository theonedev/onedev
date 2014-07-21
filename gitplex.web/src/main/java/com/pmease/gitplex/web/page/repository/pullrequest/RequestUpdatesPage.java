package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Vote;
import com.pmease.gitplex.web.component.user.UserInfoSnippet;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class RequestUpdatesPage extends RequestDetailPage {

	public RequestUpdatesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestUpdate>("updates", new LoadableDetachableModel<List<PullRequestUpdate>>() {

			@Override
			public List<PullRequestUpdate> load() {
				List<PullRequestUpdate> updates = new ArrayList<>();
				updates.addAll(getPullRequest().getSortedUpdates());
				Collections.reverse(updates);
				return updates;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestUpdate> item) {
				PullRequestUpdate update = item.getModelObject();

				User user = update.getUser();
				item.add(new UserInfoSnippet("updater", new UserModel(user)) {
					
					@Override
					protected Component newInfoLine(String componentId) {
						Fragment fragment = new Fragment(componentId, "updateInfoFrag", RequestUpdatesPage.this);

						PullRequestUpdate update = item.getModelObject();
						List<PullRequestUpdate> allUpdates = update.getRequest().getSortedUpdates();
						int index = allUpdates.indexOf(update);
						String baseCommit;
						if (index == allUpdates.size()-1)
							baseCommit = update.getRequest().getBaseCommit();
						else
							baseCommit = allUpdates.get(index+1).getHeadCommit();
						PageParameters params = RequestChangesPage.params4(
								update.getRequest(), baseCommit, update.getHeadCommit());
						Link<Void> updateLink = new BookmarkablePageLink<Void>("updateLink", RequestChangesPage.class, params);
						updateLink.add(new Label("updateNo", allUpdates.size() - allUpdates.indexOf(update)));
						fragment.add(updateLink);
						fragment.add(new Label("date", DateUtils.formatAge(update.getDate())));
						
						return fragment;
					}
				});
				
				item.add(new ListView<Vote>("votes", new LoadableDetachableModel<List<Vote>>() {

					@Override
					protected List<Vote> load() {
						List<Vote> votes = new ArrayList<>(item.getModelObject().getVotes());
						Collections.sort(votes, new Comparator<Vote>() {

							@Override
							public int compare(Vote vote1, Vote vote2) {
								return vote1.getDate().compareTo(vote2.getDate());
							}
							
						});
						return votes;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Vote> item) {
						Vote vote = item.getModelObject();

						item.add(new UserInfoSnippet("voter", new UserModel(vote.getVoter())) {
							
							@Override
							protected Component newInfoLine(String componentId) {
								Fragment fragment = new Fragment(componentId, "voteInfoFrag", RequestUpdatesPage.this);

								Vote vote = item.getModelObject();
								if (vote.getResult() == Vote.Result.APPROVE)
									fragment.add(new Label("vote", "Approved").add(AttributeModifier.append("class", " label-success")));
								else
									fragment.add(new Label("vote", "Disapproved").add(AttributeModifier.append("class", " label-danger")));
								
								Component commentIndicator = new WebComponent("comment");
								if (vote.getComment() != null) {
									commentIndicator.add(new TooltipBehavior(Model.of(vote.getComment())));
								} else {
									commentIndicator.setVisible(vote.getComment() != null);
								}
								fragment.add(commentIndicator);
								fragment.add(new Label("date", DateUtils.formatAge(vote.getDate())));
								
								return fragment;
							}
							
						});
					}
					
				});

				item.add(new UpdateCommitsPanel("detail", item.getModel()));
			}
			
		});		
	}

}
