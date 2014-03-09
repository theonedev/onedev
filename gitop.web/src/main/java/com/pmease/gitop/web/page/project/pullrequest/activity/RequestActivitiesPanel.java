package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Vote;

@SuppressWarnings("serial")
public class RequestActivitiesPanel extends Panel {

	public RequestActivitiesPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestActivity>("activities", new AbstractReadOnlyModel<List<PullRequestActivity>>() {

			@Override
			public List<PullRequestActivity> getObject() {
				PullRequest request = getPullRequest();
				List<PullRequestActivity> activities = new ArrayList<>();

				activities.add(new OpenPullRequest(request));
				
				/*
				for (PullRequestComment comment: request.getComments()) {
					activities.add(new CommentPullRequest(comment));
				}
				*/
				
				for (PullRequestUpdate update: request.getUpdates()) {
					//activities.add(new UpdatePullRequest(update));
					for (Vote vote: update.getVotes()) {
						activities.add(new VotePullRequest(vote));
					}
				}
				
				if (!request.isOpen())
					activities.add(new ClosePullRequest(request));

				/*
				Collections.sort(activities, new Comparator<PullRequestActivity>() {

					@Override
					public int compare(PullRequestActivity o1, PullRequestActivity o2) {
						return o1.getDate().compareTo(o2.getDate());
					}
					
				});
				*/
				return activities;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestActivity> item) {
				item.add(item.getModelObject().render("activity"));
			}
			
		});
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject(); 
	}
	
}
