package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.project.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestReviewManager extends BaseEntityManager<PullRequestReview> 
		implements PullRequestReviewManager {

	private final PullRequestChangeManager changeManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestReviewManager(Dao dao, PullRequestChangeManager changeManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);
		this.changeManager = changeManager;
		this.listenerRegistry = listenerRegistry;
	}

 	@Transactional
	@Override
	public void save(PullRequestReview review) {
 		review.setDirty(false);
		super.save(review);
		
		if (review.getStatus() == Status.PENDING) {
			listenerRegistry.post(new PullRequestReviewRequested(
					SecurityUtils.getUser(), review.getStatusDate(), 
					review.getRequest(), review.getUser()));
		} else if (review.getStatus() == Status.EXCLUDED) {
			listenerRegistry.post(new PullRequestReviewerRemoved(
					SecurityUtils.getUser(), review.getStatusDate(), 
					review.getRequest(), review.getUser()));
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
	public void review(PullRequest request, boolean approved, String note) {
		User user = SecurityUtils.getUser();
		PullRequestReview review = request.getReview(user);
		Preconditions.checkState(review != null && review.getStatus() == PullRequestReview.Status.PENDING);
		if (approved)
			review.setStatus(PullRequestReview.Status.APPROVED);
		else
			review.setStatus(PullRequestReview.Status.REQUESTED_FOR_CHANGES);
			
		save(review);
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(review.getStatusDate());
		change.setRequest(request);
		change.setUser(user);
		if (approved)
			change.setData(new PullRequestApproveData());
		else
			change.setData(new PullRequestRequestedForChangesData());
		
		changeManager.save(change, note);
	}

}
