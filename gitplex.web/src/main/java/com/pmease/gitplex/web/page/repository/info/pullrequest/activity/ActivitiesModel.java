package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Vote;

@SuppressWarnings("serial")
public abstract class ActivitiesModel extends LoadableDetachableModel<List<PullRequestActivity>> {

	@Override
	protected List<PullRequestActivity> load() {
		PullRequest request = getPullRequest();
		List<PullRequestActivity> activities = new ArrayList<>();

		activities.add(new OpenPullRequest(request));
		
		for (PullRequestComment comment: request.getRequestComments()) {
			activities.add(new CommentPullRequest(comment));
		}
		
		for (PullRequestUpdate update: request.getUpdates()) {
			activities.add(new UpdatePullRequest(update));
			for (Vote vote: update.getVotes()) {
				activities.add(new VotePullRequest(vote));
			}
		}
		
		if (!request.isOpen())
			activities.add(new ClosePullRequest(request));

		Collections.sort(activities, new Comparator<PullRequestActivity>() {

			@Override
			public int compare(PullRequestActivity o1, PullRequestActivity o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
			
		});
		return activities;
	}

	protected abstract PullRequest getPullRequest();
}
