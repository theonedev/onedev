package com.pmease.gitop.web.page.repository.pullrequest;

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
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.NullableUserLink;
import com.pmease.gitop.web.component.link.UserLink;
import com.pmease.gitop.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

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
				item.add(new NullableUserLink("userAvatar", user, Mode.AVATAR));
				item.add(new NullableUserLink("userName", user, Mode.NAME));
				
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
				item.add(updateLink);
				item.add(new Label("date", DateUtils.formatAge(update.getDate())));
				item.add(new UpdateCommitsPanel("detail", item.getModel()));

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
					protected void populateItem(ListItem<Vote> item) {
						Vote vote = item.getModelObject();
						UserLink userLink = new UserLink("user", vote.getVoter());
						item.add(userLink);

						String popoverTitle;
						if (vote.getResult() == Vote.Result.APPROVE) {
							item.add(new WebComponent("result").add(AttributeModifier.append("class", " fa-smile-o")));
							popoverTitle = "Approved";
						} else {
							item.add(new WebComponent("result").add(AttributeModifier.append("class", " fa-frown-o")));
							popoverTitle = "Disapproved";
						}
						
						Component commentIndicator = new WebComponent("comment");
						commentIndicator.setVisible(vote.getComment() != null);
						item.add(commentIndicator);
						
						item.add(new PopoverBehavior(
								Model.of(popoverTitle), 
								Model.of(vote.getComment() != null? vote.getComment(): "<i>No comment</i>"), 
								new PopoverConfig().withHoverTrigger().withPlacement(Placement.top)));
					}
					
				});
			}
			
		});		
	}

}
