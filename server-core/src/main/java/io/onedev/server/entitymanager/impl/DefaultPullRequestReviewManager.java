package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewerAddData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewerRemoveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestReviewManager extends AbstractEntityManager<PullRequestReview> implements PullRequestReviewManager {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	@Inject
	public DefaultPullRequestReviewManager(Dao dao, PullRequestManager pullRequestManager, PullRequestChangeManager pullRequestChangeManager) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.pullRequestChangeManager = pullRequestChangeManager;
	}

	@Transactional
	@Override
	public void review(PullRequestReview review) {
		ReviewResult result = review.getResult();
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(review.getRequest());
		if (result.isApproved()) 
			change.setData(new PullRequestApproveData(result.getComment()));
		else 
			change.setData(new PullRequestRequestedForChangesData(result.getComment()));
		change.setUser(review.getUser());
		pullRequestChangeManager.save(change);
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
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setRequest(request);
			change.setUser(SecurityUtils.getUser());
			change.setData(new PullRequestReviewerRemoveData(reviewer.getDisplayName()));
			pullRequestChangeManager.save(change);
			return true;
		}
	}

	@Transactional
	@Override
	public void addReviewer(PullRequestReview review) {
		save(review);
		
		PullRequest request = review.getRequest();
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestReviewerAddData(review.getUser().getDisplayName()));
		change.setUser(SecurityUtils.getUser());
		pullRequestChangeManager.save(change);
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
