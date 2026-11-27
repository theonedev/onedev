package io.onedev.server.service;

import java.util.Collection;

import org.apache.shiro.subject.Subject;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;

public interface PullRequestReviewService extends EntityService<PullRequestReview> {

	/** Apply branch and file review requirements and record any shortage as a merge blocker. Draft changes stay in memory. */
	void reconcile(User actor, PullRequest request, Collection<User> proposedExclusions);

	/** Check the proposed reviewer's eligibility and add them, preserving any active decision. The caller authorizes the actor. */
	void addReviewer(User actor, PullRequest request, User reviewer);

	/** Request another decision from an active reviewer. Repeating a pending request has no effect. */
	void requestReview(User actor, PullRequest request, User reviewer);

	void approve(User reviewer, PullRequestUpdate update, @Nullable String note);

	void requestChanges(User reviewer, PullRequestUpdate update, @Nullable String note);

	void populateReviews(Collection<PullRequest> requests);

}
