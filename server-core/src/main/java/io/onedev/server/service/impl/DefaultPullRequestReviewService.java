package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.project.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.exception.PullRequestReviewRejectedException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestReviewService;

@Singleton
public class DefaultPullRequestReviewService extends BaseEntityService<PullRequestReview>
		implements PullRequestReviewService {

	@Inject
	private PullRequestChangeService changeService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void createOrUpdate(User user, PullRequestReview review) {
 		review.setDirty(false);
		dao.persist(review);
		
		if (review.getStatus() == Status.PENDING) {
			listenerRegistry.post(new PullRequestReviewRequested(
					user, review.getStatusDate(), review.getRequest(), review.getUser()));
		} else if (review.getStatus() == Status.EXCLUDED) {
			listenerRegistry.post(new PullRequestReviewerRemoved(
					user, review.getStatusDate(), review.getRequest(), review.getUser()));
		}
	}

	@Sessional
	@Override
	public void populateReviews(Collection<PullRequest> requests) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestReview> query = builder.createQuery(PullRequestReview.class);
		
		Root<PullRequestReview> root = query.from(PullRequestReview.class);
		query.select(root);
		Join<PullRequest, PullRequest> join = root.join(PullRequestReview.PROP_REQUEST);
		query.where(join.in(requests));
		
		for (PullRequest request: requests) 
			request.setReviews(new ArrayList<>());
		
		for (PullRequestReview review: getSession().createQuery(query).getResultList())
			review.getRequest().getReviews().add(review);
	}
 	
	@Transactional
	@Override
	public void review(User user, PullRequest request, boolean approved, String note) {
		PullRequestReview review = request.getReview(user);
		if (review == null || review.getStatus() == PullRequestReview.Status.EXCLUDED)
			throw new PullRequestReviewRejectedException("You are not reviewer of this pull request");
		if (approved)
			review.setStatus(PullRequestReview.Status.APPROVED);
		else
			review.setStatus(PullRequestReview.Status.REQUESTED_FOR_CHANGES);
			
		createOrUpdate(user, review);
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(review.getStatusDate());
		change.setRequest(request);
		change.setUser(user);
		if (approved)
			change.setData(new PullRequestApproveData());
		else
			change.setData(new PullRequestRequestedForChangesData());
		
		changeService.create(change, note);
	}

}
