package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.PullRequestActionManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.model.support.pullrequest.actiondata.AddedReviewerData;
import io.onedev.server.model.support.pullrequest.actiondata.ApprovedData;
import io.onedev.server.model.support.pullrequest.actiondata.RemovedReviewerData;
import io.onedev.server.model.support.pullrequest.actiondata.RequestedForChangesData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestReviewManager extends AbstractEntityManager<PullRequestReview> implements PullRequestReviewManager {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestActionManager pullRequestActionManager;
	
	@Inject
	public DefaultPullRequestReviewManager(Dao dao, PullRequestManager pullRequestManager, PullRequestActionManager pullRequestActionManager) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.pullRequestActionManager = pullRequestActionManager;
	}

	@Transactional
	@Override
	public void review(PullRequestReview review) {
		ReviewResult result = review.getResult();
		PullRequestAction action = new PullRequestAction();
		action.setDate(new Date());
		action.setRequest(review.getRequest());
		if (result.isApproved()) 
			action.setData(new ApprovedData(result.getComment()));
		else 
			action.setData(new RequestedForChangesData(result.getComment()));
		action.setUser(review.getUser());
		pullRequestActionManager.save(action);
		save(review);
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
	public boolean excludeReviewer(PullRequestReview review) {
		PullRequest request = review.getRequest();
		
		User reviewer = review.getUser();
		pullRequestManager.checkQuality(request);
		if (request.getReview(reviewer).getExcludeDate() == null) {
			return false;
		} else {
			save(review);
			
			PullRequestAction action = new PullRequestAction();
			action.setDate(new Date());
			action.setRequest(request);
			action.setUser(SecurityUtils.getUser());
			action.setData(new RemovedReviewerData(reviewer.getDisplayName()));
			pullRequestActionManager.save(action);
			return true;
		}
	}

	@Transactional
	@Override
	public void addReviewer(PullRequestReview review) {
		save(review);
		
		PullRequest request = review.getRequest();
		
		PullRequestAction action = new PullRequestAction();
		action.setDate(new Date());
		action.setRequest(request);
		action.setData(new AddedReviewerData(review.getUser().getDisplayName()));
		action.setUser(SecurityUtils.getUser());
		pullRequestActionManager.save(action);
	}

	@Transactional
	@Override
	public void saveReviews(PullRequest request) {
		Collection<Long> ids = new HashSet<>();
		for (PullRequestReview review: request.getReviews()) {
			save(review);
			ids.add(review.getId());
		}
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery("delete from PullRequestReview where request=:request and id not in (:ids)");
			query.setParameter("request", request);
			query.setParameter("ids", ids);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from PullRequestReview where request=:request");
			query.setParameter("request", request);
			query.executeUpdate();
		}
	}
}
