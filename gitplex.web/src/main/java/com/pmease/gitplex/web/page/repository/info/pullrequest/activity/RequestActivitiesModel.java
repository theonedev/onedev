package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestAction.Approve;
import com.pmease.gitplex.core.model.PullRequestAction.Disapprove;
import com.pmease.gitplex.core.model.PullRequestAction.Discard;
import com.pmease.gitplex.core.model.PullRequestAction.Integrate;
import com.pmease.gitplex.core.model.PullRequestAudit;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;

@SuppressWarnings("serial")
public abstract class RequestActivitiesModel extends LoadableDetachableModel<List<PullRequestActivity>> {

	@Override
	protected List<PullRequestActivity> load() {
		PullRequest request = getPullRequest();
		List<PullRequestActivity> activities = new ArrayList<>();

		activities.add(new OpenPullRequest(request));

		for (PullRequestUpdate update: request.getUpdates())
			activities.add(new UpdatePullRequest(update));
		
		for (PullRequestComment comment: request.getComments()) 
			activities.add(new CommentPullRequest(comment));
		
		for (PullRequestAudit audit: request.getAudits()) {
			if (audit.getAction() instanceof Integrate) {
				Integrate integrate = (Integrate) audit.getAction();
				activities.add(new IntegratePullRequest(audit.getUser(), audit.getDate(), 
						integrate.getReason()));
			} else if (audit.getAction() instanceof Discard) { 
				activities.add(new DiscardPullRequest(audit.getUser(), audit.getDate()));
			} else if (audit.getAction() instanceof Approve) {
				activities.add(new ApprovePullRequest(audit.getUser(), audit.getDate()));
			} else if (audit.getAction() instanceof Disapprove) {
				activities.add(new DisapprovePullRequest(audit.getUser(), audit.getDate()));
			} else {
				throw new IllegalStateException("Unexpected audit action: " + audit.getAction());
			}
		}
		
		Collections.sort(activities, new Comparator<PullRequestActivity>() {

			@Override
			public int compare(PullRequestActivity o1, PullRequestActivity o2) {
				if (o1.getDate().before(o2.getDate()))
					return -1;
				else if (o1.getDate().after(o2.getDate()))
					return 1;
				else if (o1 instanceof OpenPullRequest || o1 instanceof CommentPullRequest)
					return -1;
				else
					return 1;
			}
			
		});
		return activities;
	}

	protected abstract PullRequest getPullRequest();
}
