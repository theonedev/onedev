package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestNotificationManager;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.PullRequestNotification;
import com.pmease.gitplex.core.model.PullRequestNotification.Task;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;

@Singleton
public class DefaultPullRequestNotificationManager implements PullRequestNotificationManager {

	private final Dao dao;
	
	private final MailManager mailManager;
	
	@Inject
	public DefaultPullRequestNotificationManager(Dao dao, MailManager mailManager) {
		this.dao = dao;
		this.mailManager = mailManager;
	}

	@Override
	public void onOpened(PullRequest request) {
		if (request.getAssignee() != null && !request.getAssignee().equals(request.getSubmitter()))
			onAssigned(request);
	}

	@Transactional
	@Override
	public void onUpdated(PullRequest request) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and task=:task");
		query.setParameter("request", request);
		query.setParameter("task", PullRequestNotification.Task.UPDATE);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onCommented(PullRequestComment comment) {
	}

	@Transactional
	@Override
	public void onCommentReplied(PullRequestCommentReply reply) {
	}

	@Transactional
	@Override
	public void onReviewed(Review review) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and user=:user and task=:task");
		query.setParameter("request", review.getUpdate().getRequest());
		query.setParameter("user", review.getReviewer());
		query.setParameter("task", PullRequestNotification.Task.REVIEW);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onAssigned(PullRequest request) {
		Preconditions.checkNotNull(request.getAssignee());
		
		if (request.getStatus() == PullRequest.Status.PENDING_INTEGRATE) {
			Query query = dao.getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and task=:task");
			query.setParameter("request", request);
			query.setParameter("task", PullRequestNotification.Task.INTEGRATE);
			query.executeUpdate();
			
			PullRequestNotification notification = new PullRequestNotification();
			notification.setRequest(request);
			notification.setUser(request.getAssignee());
			notification.setTask(PullRequestNotification.Task.INTEGRATE);
			dao.persist(notification);
			
			mailManager.sendMail(request.getAssignee(), "Please integrate pull request: " + request.getId(), null);
		} else {
			mailManager.sendMail(request.getAssignee(), "You are assigned with pull request: " + request.getId(), null);
		}
	}

	@Override
	public void onVerified(PullRequest request) {
	}

	@Transactional
	@Override
	public void onIntegrated(PullRequest request) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and task is not null");
		query.setParameter("request", request);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onDiscarded(PullRequest request) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and task is not null");
		query.setParameter("request", request);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		IntegrationPreview preview = request.getLastIntegrationPreview();
		Preconditions.checkNotNull(preview);
		if (preview.getIntegrated() == null) {
			notifyUpdate(request, true);
			mailManager.sendMail(request.getSubmitter(), "Please update pull request to resolve conflicts: " + request.getId(), null);
		}
	}

	@Transactional
	@Override
	public void notifyReview(ReviewInvitation invitation) {
		if (!invitation.isPreferred()) {
			Query query = dao.getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and user=:user and task=:task");
			query.setParameter("request", invitation.getRequest());
			query.setParameter("user", invitation.getReviewer());
			query.setParameter("task", PullRequestNotification.Task.REVIEW);
			query.executeUpdate();
		} else {
			PullRequestNotification notification = new PullRequestNotification();
			notification.setRequest(invitation.getRequest());
			notification.setUser(invitation.getReviewer());
			notification.setTask(PullRequestNotification.Task.REVIEW);
			EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
			criteria.add(Restrictions.eq("request", notification.getRequest()))
					.add(Restrictions.eq("user", notification.getUser()))
					.add(Restrictions.eq("task", notification.getTask()));
			if (dao.find(criteria) == null) {
				dao.persist(notification);
				mailManager.sendMail(notification.getUser(), "Please review pull request: " + invitation.getRequest().getId(), null);
			}
		}
	}

	@Transactional
	@Override
	public void notifyIntegration(PullRequest request) {
		Preconditions.checkNotNull(request.getAssignee());
		
		PullRequestNotification notification = new PullRequestNotification();
		notification.setRequest(request);
		notification.setUser(request.getAssignee());
		notification.setTask(PullRequestNotification.Task.INTEGRATE);
		EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
		criteria.add(Restrictions.eq("request", notification.getRequest()))
				.add(Restrictions.eq("user", notification.getUser()))
				.add(Restrictions.eq("task", notification.getTask()));
		if (dao.find(criteria) == null) {
			dao.persist(notification);
			mailManager.sendMail(notification.getUser(), "Please integrate pull request: " + request.getId(), null);
		}
	}

	@Transactional
	@Override
	public void notifyUpdate(PullRequest request, boolean noMail) {
		PullRequestNotification notification = new PullRequestNotification();
		notification.setRequest(request);
		notification.setUser(request.getSubmitter());
		notification.setTask(PullRequestNotification.Task.UPDATE);
		EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
		criteria.add(Restrictions.eq("request", notification.getRequest()))
				.add(Restrictions.eq("user", notification.getUser()))
				.add(Restrictions.eq("task", notification.getTask()));
		if (dao.find(criteria) == null) {
			dao.persist(notification);
			if (!noMail)
				mailManager.sendMail(notification.getUser(), "Please update pull request: " + request.getId(), null);
		}
	}

	@Transactional
	@Override
	public void pendingApproval(PullRequest request) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and (task=:update or task=:integrate)");
		query.setParameter("request", request);
		query.setParameter("update", Task.UPDATE);
		query.setParameter("integrate", Task.INTEGRATE);
		query.executeUpdate();
	}

}