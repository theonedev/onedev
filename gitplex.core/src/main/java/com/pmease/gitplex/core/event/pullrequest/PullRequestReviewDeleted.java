package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestReview;

@Editable(name="deleted review", icon="fa fa-times")
public class PullRequestReviewDeleted extends PullRequestStatusChangeEvent {

	private final PullRequestReview review;
	
	public PullRequestReviewDeleted(PullRequestReview review, Account user, String note) {
		super(review.getUpdate().getRequest(), user, note);
		this.review = review;
	}

	public PullRequestReview getReview() {
		return review;
	}

}
