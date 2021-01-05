package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
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
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	@Inject
	public DefaultPullRequestReviewManager(Dao dao, 
			PullRequestManager pullRequestManager, 
			PullRequestChangeManager pullRequestChangeManager) {
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
	public boolean removeReviewer(PullRequestReview review, List<User> unpreferableReviewers) {
		PullRequest request = review.getRequest();
		User reviewer = review.getUser();
		request.getReviews().remove(review);
		request.setReviews(request.getReviews());
		
		pullRequestManager.checkReviews(request, unpreferableReviewers);
		
		if (request.isNew()) {
			return request.getReview(reviewer) == null;
		} else {
			saveReviews(request);
			if (request.getReview(reviewer) == null) {
				PullRequestChange change = new PullRequestChange();
				change.setDate(new Date());
				change.setRequest(request);
				change.setUser(SecurityUtils.getUser());
				change.setData(new PullRequestReviewerRemoveData(reviewer.getDisplayName()));
				pullRequestChangeManager.save(change);
				return true;
			} else {
				return false;
			}
		}
	}

	@Transactional
	@Override
	public void addReviewer(PullRequestReview review) {
		save(review);
		
		PullRequest request = review.getRequest();
		request.getReviews().add(review);
		request.setReviews(request.getReviews());
		
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
		Collection<User> reviewers = request.getReviews().stream()
				.filter(it->!it.isNew()).map(it->it.getUser()).collect(Collectors.toList());
		if (!reviewers.isEmpty()) {
			Query query = getSession().createQuery("delete from PullRequestReview where request=:request and user not in (:reviewers)");
			query.setParameter("request", request);
			query.setParameter("reviewers", reviewers);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from PullRequestReview where request=:request");
			query.setParameter("request", request);
			query.executeUpdate();
		}
		for (PullRequestReview review: request.getReviews())
			save(review);
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
