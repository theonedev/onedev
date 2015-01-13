package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.model.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitplex.core.model.PullRequestNotification.Task.INTEGRATE;
import static com.pmease.gitplex.core.model.PullRequestNotification.Task.REVIEW;
import static com.pmease.gitplex.core.model.PullRequestNotification.Task.UPDATE;

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
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;

/**
 * This class manages pull request task notifications and send email to relevant users. This 
 * notification does not correlate with pull request watch notifications and can not be 
 * suppressed. 
 *   
 * @author robin
 *
 */
@Singleton
public class DefaultPullRequestNotificationManager implements PullRequestNotificationManager {

	private final Dao dao;
	
	private final MailManager mailManager;
	
	@Inject
	public DefaultPullRequestNotificationManager(Dao dao, MailManager mailManager) {
		this.dao = dao;
		this.mailManager = mailManager;
	}

	@Transactional
	@Override
	public void onOpened(PullRequest request) {
		if (request.getAssignee() != null && !request.getAssignee().equals(request.getSubmitter()))
			onAssigned(request);
	}

	@Transactional
	@Override
	public void onUpdated(PullRequestUpdate update) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and task=:task");
		query.setParameter("request", update.getRequest());
		query.setParameter("task", UPDATE);
		query.executeUpdate();
	}

	@Override
	public void onCommented(PullRequestComment comment) {
	}

	@Override
	public void onCommentReplied(PullRequestCommentReply reply) {
	}

	@Transactional
	@Override
	public void onReviewed(Review review, String comment) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and user=:user and task=:task");
		query.setParameter("request", review.getUpdate().getRequest());
		query.setParameter("user", review.getReviewer());
		query.setParameter("task", REVIEW);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onAssigned(PullRequest request) {
		Preconditions.checkNotNull(request.getAssignee());
		
		if (request.getStatus() == PENDING_INTEGRATE) {
			Query query = dao.getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and task=:task");
			query.setParameter("request", request);
			query.setParameter("task", INTEGRATE);
			query.executeUpdate();
			
			PullRequestNotification notification = new PullRequestNotification();
			notification.setRequest(request);
			notification.setUser(request.getAssignee());
			notification.setTask(INTEGRATE);
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
	public void onIntegrated(PullRequest request, User user, String comment) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request");
		query.setParameter("request", request);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onDiscarded(PullRequest request, User user, String comment) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request");
		query.setParameter("request", request);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		IntegrationPreview preview = request.getLastIntegrationPreview();
		Preconditions.checkNotNull(preview);
		
		if (preview.getIntegrated() != null) {
			if (request.getStatus() == PENDING_INTEGRATE && request.getAssignee() != null)
				requestIntegration(request);
		} else {
			mailManager.sendMail(request.getSubmitter(), "Please update pull request to resolve conflicts: " + request.getId(), null);
		}
	}

	@Transactional
	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
		if (!invitation.isPreferred()) {
			Query query = dao.getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and user=:user and task=:task");
			query.setParameter("request", invitation.getRequest());
			query.setParameter("user", invitation.getReviewer());
			query.setParameter("task", REVIEW);
			query.executeUpdate();
		} else {
			PullRequestNotification notification = new PullRequestNotification();
			notification.setRequest(invitation.getRequest());
			notification.setUser(invitation.getReviewer());
			notification.setTask(REVIEW);
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
	public void pendingApproval(PullRequest request) {
		Query query = dao.getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and (task=:update or task=:integrate)");
		query.setParameter("request", request);
		query.setParameter("update", UPDATE);
		query.setParameter("integrate", INTEGRATE);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onMentioned(PullRequest request, User user, String content) {
		mailManager.sendMail(user, "You've mentioned in pull request #" + request.getId() + " (" + request.getTitle() + ")", content);
	}

	@Transactional
	@Override
	public void onReopened(PullRequest request, User user, String comment) {
		if (request.getAssignee() != null && !request.getAssignee().equals(user))
			onAssigned(request);
	}

	private void requestIntegration(PullRequest request) {
		PullRequestNotification notification = new PullRequestNotification();
		notification.setRequest(request);
		notification.setUser(request.getAssignee());
		notification.setTask(INTEGRATE);
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
	public void pendingIntegration(PullRequest request) {
		IntegrationPreview preview = request.getLastIntegrationPreview();
		if (preview != null && preview.getIntegrated() != null && request.getAssignee() != null) 
			requestIntegration(request);
	}

	@Transactional
	@Override
	public void pendingUpdate(PullRequest request) {
		PullRequestNotification notification = new PullRequestNotification();
		notification.setRequest(request);
		notification.setUser(request.getSubmitter());
		notification.setTask(UPDATE);
		EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
		criteria.add(Restrictions.eq("request", notification.getRequest()))
				.add(Restrictions.eq("user", notification.getUser()))
				.add(Restrictions.eq("task", notification.getTask()));
		if (dao.find(criteria) == null)
			dao.persist(notification);
	}

}