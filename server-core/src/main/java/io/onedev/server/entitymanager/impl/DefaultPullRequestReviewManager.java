package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Lists;

import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.exception.ReviewerRequiredException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewWithdrawData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewerAddData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewerRemoveData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestReviewManager extends BaseEntityManager<PullRequestReview> 
		implements PullRequestReviewManager {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestChangeManager changeManager;
	
	@Inject
	public DefaultPullRequestReviewManager(Dao dao, PullRequestManager pullRequestManager, 
			PullRequestChangeManager changeManager) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.changeManager = changeManager;
	}

	@Override
	public PullRequestReview find(PullRequest request, User user, String commit) {
		EntityCriteria<PullRequestReview> criteria = EntityCriteria.of(PullRequestReview.class);
		criteria.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit));
		return find(criteria);
	}	
	
	@Transactional
	@Override
	public void delete(PullRequestReview review) {
		PullRequest request = review.getRequest();
		request.getReviews().remove(review);
		User reviewer = review.getUser();
		pullRequestManager.checkReviews(request, Lists.newArrayList(reviewer));
		if (request.isNew()) {
			if (request.getReview(reviewer) != null)
				throw new ReviewerRequiredException(reviewer);
		} else if (request.getReview(reviewer) == null) {
			dao.remove(review);
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setRequest(review.getRequest());
			change.setData(new PullRequestReviewerRemoveData(reviewer));
			change.setUser(SecurityUtils.getUser());
			changeManager.save(change);
			
			for (PullRequestReview eachReview: request.getReviews()) {
				if (eachReview.isNew())
					save(eachReview);
			}
		} else {
			throw new ReviewerRequiredException(reviewer);
		}
	}

 	@Transactional
	@Override
	public void save(PullRequestReview review) {
 		boolean isNew = review.isNew();
 		review.setDirty(false);
		super.save(review);

		PullRequestChange change = new PullRequestChange();
		if (isNew) {
			change.setDate(new Date());
			change.setRequest(review.getRequest());
			change.setData(new PullRequestReviewerAddData(review.getUser()));
			change.setUser(SecurityUtils.getUser());
			changeManager.save(change);
		} else {
			ReviewResult result = review.getResult();
			
			if (result != null) {
				change.setDate(new Date());
				change.setRequest(review.getRequest());
				
				change.setComment(result.getComment());
				if (Boolean.TRUE.equals(result.getApproved())) 
					change.setData(new PullRequestApproveData());
				else if (Boolean.FALSE.equals(result.getApproved()))
					change.setData(new PullRequestRequestedForChangesData());
				else
					change.setData(new PullRequestReviewWithdrawData());
				change.setUser(review.getUser());
				changeManager.save(change);
			}
		}
	}

	@Sessional
	@Override
	public void populateReviews(Collection<PullRequest> requests) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestReview> query = builder.createQuery(PullRequestReview.class);
		
		Root<PullRequestReview> root = query.from(PullRequestReview.class);
		query.select(root);
		root.join(PullRequestReview.PROP_REQUEST);
		
		query.where(root.get(PullRequestReview.PROP_REQUEST).in(requests));
		
		for (PullRequest request: requests)
			request.setReviews(new ArrayList<>());
		
		for (PullRequestReview review: getSession().createQuery(query).getResultList())
			review.getRequest().getReviews().add(review);
	}
	
}
