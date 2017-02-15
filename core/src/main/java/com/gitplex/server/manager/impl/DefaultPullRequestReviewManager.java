package com.gitplex.server.manager.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReview;
import com.gitplex.server.entity.PullRequestStatusChange;
import com.gitplex.server.entity.PullRequestUpdate;
import com.gitplex.server.entity.PullRequestStatusChange.Type;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestReviewManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestReviewManager extends AbstractEntityManager<PullRequestReview> implements PullRequestReviewManager {

	private final AccountManager accountManager;

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	@Inject
	public DefaultPullRequestReviewManager(Dao dao, AccountManager accountManager, PullRequestManager pullRequestManager, 
			PullRequestStatusChangeManager pullRequestStatusChangeManager) {
		super(dao);
		
		this.accountManager = accountManager;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestStatusChangeManager = pullRequestStatusChangeManager;
	}

	@Sessional
	@Override
	public PullRequestReview find(Account user, PullRequestUpdate update) {
		return find(EntityCriteria.of(PullRequestReview.class)
				.add(Restrictions.eq("user", user)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void review(PullRequest request, PullRequestReview.Result result, String note) {
		Account user = accountManager.getCurrent();
		Date date = new Date();
		
		PullRequestReview review = new PullRequestReview();
		review.setUpdate(request.getLatestUpdate());
		review.setResult(result);
		review.setUser(user);
		review.setDate(date);
		save(review);	

		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(date);
		statusChange.setNote(note);
		statusChange.setRequest(request);
		if (result == PullRequestReview.Result.APPROVE) {
			statusChange.setType(Type.APPROVED);
		} else {
			statusChange.setType(Type.DISAPPROVED);
		}
		statusChange.setUser(user);
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
	}

	@Transactional
	@Override
	public void delete(PullRequestReview entity) {
		super.delete(entity);
		
		PullRequest request = entity.getUpdate().getRequest();
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setRequest(request);
		statusChange.setType(Type.REVIEW_DELETED);
		statusChange.setUser(accountManager.getCurrent());
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
	}

	@Sessional
	@Override
	public List<PullRequestReview> findAll(PullRequest request) {
		EntityCriteria<PullRequestReview> criteria = EntityCriteria.of(PullRequestReview.class);
		criteria.createCriteria("update").add(Restrictions.eq("request", request));
		criteria.addOrder(Order.asc("date"));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void deleteAll(Account user, PullRequest request) {
		for (Iterator<PullRequestReview> it = request.getReviews().iterator(); it.hasNext();) {
			PullRequestReview review = it.next();
			if (review.getUser().equals(user)) {
				delete(review);
				it.remove();
			}
		}
	}

}
