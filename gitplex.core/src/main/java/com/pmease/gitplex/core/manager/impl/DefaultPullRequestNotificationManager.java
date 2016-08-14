package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestNotification.Task.INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestNotification.Task.REVIEW;
import static com.pmease.gitplex.core.entity.PullRequestNotification.Task.UPDATE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.Listen;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReview;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.entity.PullRequestNotification;
import com.pmease.gitplex.core.event.pullrequest.PullRequestApproved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestAssigned;
import com.pmease.gitplex.core.event.pullrequest.PullRequestDisapproved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestDiscarded;
import com.pmease.gitplex.core.event.pullrequest.PullRequestIntegrated;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingApproval;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingIntegration;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingUpdate;
import com.pmease.gitplex.core.event.pullrequest.PullRequestReviewInvitationChanged;
import com.pmease.gitplex.core.event.pullrequest.PullRequestUpdated;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestNotificationManager;
import com.pmease.gitplex.core.manager.UrlManager;

/**
 * This class manages pull request tasks and send task notifications if necessary. 
 * Unlike watch based notifications, task notifications can not be suppressed. 
 *   
 * @author robin
 *
 */
@Singleton
public class DefaultPullRequestNotificationManager extends AbstractEntityManager<PullRequestNotification> 
		implements PullRequestNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	@Inject
	public DefaultPullRequestNotificationManager(Dao dao, MailManager mailManager, UrlManager urlManager) {
		super(dao);
		
		this.mailManager = mailManager;
		this.urlManager = urlManager;
	}

	@Transactional
	@Listen
	public void on(PullRequestUpdated event) {
		Query query = getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and task=:task");
		query.setParameter("request", event.getRequest());
		query.setParameter("task", UPDATE);
		query.executeUpdate();
	}

	@Transactional
	@Listen
	public void on(PullRequestAssigned event) {
		PullRequest request = event.getRequest();
		Preconditions.checkNotNull(request.getAssignee());
		
		if (request.getStatus() == PENDING_INTEGRATE) {  
			Query query = getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and task=:task and user!=:user");
			query.setParameter("request", request);
			query.setParameter("task", INTEGRATE);
			query.setParameter("user", request.getAssignee());
			query.executeUpdate();
			
			requestIntegration(request);
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestIntegrated event) {
		onClosed(event.getRequest());
	}

	@Transactional
	@Listen
	public void on(PullRequestDiscarded event) {
		onClosed(event.getRequest());
	}
	
	private void onClosed(PullRequest request) {
		Query query = getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request");
		query.setParameter("request", request);
		query.executeUpdate();
	}

	@Transactional
	@Listen
	public void on(PullRequestReviewInvitationChanged event) {
		PullRequestReviewInvitation invitation = event.getInvitation();
		PullRequest request = invitation.getRequest();
		Account user = invitation.getUser();
		if (invitation.getStatus() == PullRequestReviewInvitation.Status.EXCLUDED) {
			Query query = getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and user=:user and task=:task");
			query.setParameter("request", request);
			query.setParameter("user", user);
			query.setParameter("task", REVIEW);
			query.executeUpdate();
		} else if (invitation.getStatus() == PullRequestReviewInvitation.Status.ADDED_BY_RULE) {
			PullRequestNotification task = new PullRequestNotification();
			task.setRequest(request);
			task.setUser(user);
			task.setTask(REVIEW);
			EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
			criteria.add(Restrictions.eq("request", request))
					.add(Restrictions.eq("user", user))
					.add(Restrictions.eq("task", task.getTask()));
			if (find(criteria) == null) {
				save(task);
				
				String subject = String.format("Please review pull request #%d (%s)", 
						request.getNumber(), request.getTitle());
				String url = urlManager.urlFor(request);
				String body = String.format("You are designated to review pull request #%d (%s).<br>"
						+ "Please visit <a href='%s'>%s</a> to do the review.",
						request.getNumber(), request.getTitle(), url, url);
				mailManager.sendMailAsync(Sets.newHashSet(task.getUser()), subject, decorate(user, body));
			}
		} else {
			String subject = String.format("You are invited to review pull request #%d (%s)", 
					request.getNumber(), request.getTitle());
			String url = urlManager.urlFor(request);
			String body = String.format("You are invited to review pull request #%d (%s).<br>"
					+ "Please visit <a href='%s'>%s</a> to do the review.",
					request.getNumber(), request.getTitle(), url, url);
			mailManager.sendMailAsync(Sets.newHashSet(invitation.getUser()), subject, decorate(user, body));
		}
	}
	
	private String decorate(Account user, String body) {
		return String.format("Dear %s, "
				+ "<p style='margin: 16px 0;'>"
				+ "%s"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex", 
				user.getDisplayName(), body);
	}

	@Transactional
	@Listen
	public void on(PullRequestPendingApproval event) {
		Query query = getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and (task=:update or task=:integrate)");
		query.setParameter("request", event.getRequest());
		query.setParameter("update", UPDATE);
		query.setParameter("integrate", INTEGRATE);
		query.executeUpdate();
	}

	private void requestIntegration(PullRequest request) {
		Account user = request.getAssignee();
		PullRequestNotification task = new PullRequestNotification();
		task.setRequest(request);
		task.setUser(user);
		task.setTask(INTEGRATE);
		EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
		criteria.add(Restrictions.eq("request", task.getRequest()))
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("task", task.getTask()));
		if (find(criteria) == null) {
			save(task);
			String subject = String.format("Please integrate pull request #%d (%s)", 
					request.getNumber(), request.getTitle());
			String url = urlManager.urlFor(request);
			String body = String.format("Pull request #%d (%s) is pending integration.<br>"
					+ "Please visit <a href='%s'>%s</a> to check and integrate.", 
					request.getNumber(), request.getTitle(), url, url);
			
			mailManager.sendMailAsync(Sets.newHashSet(task.getUser()), subject, decorate(user, body));
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestPendingIntegration event) {
		PullRequest request = event.getRequest();
		if (request.getAssignee() != null) 
			requestIntegration(request);
	}

	@Transactional
	@Listen
	public void on(PullRequestPendingUpdate event) {
		PullRequest request = event.getRequest();
		if (request.getSubmitter() != null) {
			PullRequestNotification task = new PullRequestNotification();
			task.setRequest(request);
			task.setUser(request.getSubmitter());
			task.setTask(UPDATE);
			EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
			criteria.add(Restrictions.eq("request", task.getRequest()))
					.add(Restrictions.eq("user", task.getUser()))
					.add(Restrictions.eq("task", task.getTask()));
			if (find(criteria) == null) {
				save(task);
				String subject = String.format("New commits are expected in pull request #%d (%s)", 
						request.getNumber(), request.getTitle());
				String url = urlManager.urlFor(request);
				String body = String.format("Next commits are expected for next round of review in pull request #%d (%s).<br>"
						+ "Please visit <a href='%s'>%s</a> for details",
						request.getNumber(), request.getTitle(), url, url);
				mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter()), subject, 
						decorate(request.getSubmitter(), body));
			}
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestApproved event) {
		onReviewed(event.getReview());
	}

	@Transactional
	@Listen
	public void on(PullRequestDisapproved event) {
		onReviewed(event.getReview());
	}
	
	private void onReviewed(PullRequestReview review) {
		Query query = getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request and user=:user and task=:task");
		query.setParameter("request", review.getUpdate().getRequest());
		query.setParameter("user", review.getUser());
		query.setParameter("task", REVIEW);
		query.executeUpdate();
	}
	
}