package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.PullRequestReview;

@Editable(name="approved", icon="fa fa-thumbs-o-up")
public class PullRequestApproved extends PullRequestStatusChangeEvent {

	private final PullRequestReview review;

	public PullRequestApproved(PullRequestReview review, String note) {
		super(review.getUpdate().getRequest(), review.getUser(), note);
		this.review = review;
	}

	public PullRequestReview getReview() {
		return review;
	}

}
