package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.PullRequestNotification.Task.INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestNotification.Task.REVIEW;
import static com.pmease.gitplex.core.entity.PullRequestNotification.Task.UPDATE;
import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;

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
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestNotification;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestReview;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.event.pullrequest.AccountMentioned;
import com.pmease.gitplex.core.event.pullrequest.AccountMentionedInComment;
import com.pmease.gitplex.core.event.pullrequest.InvitingPullRequestReview;
import com.pmease.gitplex.core.event.pullrequest.PullRequestApproved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestAssigned;
import com.pmease.gitplex.core.event.pullrequest.PullRequestDisapproved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestDiscarded;
import com.pmease.gitplex.core.event.pullrequest.PullRequestIntegrated;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingApproval;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingIntegration;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingUpdate;
import com.pmease.gitplex.core.event.pullrequest.PullRequestUpdated;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.UrlManager;

/**
 * This class manages pull request task notifications and send email to relevant users. This 
 * notification does not correlate with pull request watch notifications and can not be 
 * suppressed. 
 *   
 * @author robin
 *
 */
@Singleton
public class DefaultPullRequestNotificationManager extends AbstractEntityManager<PullRequestNotification> 
		implements NotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public DefaultPullRequestNotificationManager(Dao dao, MailManager mailManager, 
			UrlManager urlManager, MarkdownManager markdownManager) {
		super(dao);
		
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.markdownManager = markdownManager;
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
		Query query = getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request");
		query.setParameter("request", event.getRequest());
		query.executeUpdate();
	}

	@Transactional
	@Listen
	public void on(PullRequestDiscarded event) {
		Query query = getSession().createQuery("delete from PullRequestNotification "
				+ "where request=:request");
		query.setParameter("request", event.getRequest());
		query.executeUpdate();
	}

	@Transactional
	@Listen
	public void on(InvitingPullRequestReview event) {
		PullRequestReviewInvitation invitation = event.getInvitation();
		PullRequest request = invitation.getRequest();
		Account user = invitation.getUser();
		if (invitation.isExcluded()) {
			Query query = getSession().createQuery("delete from PullRequestNotification "
					+ "where request=:request and user=:user and task=:task");
			query.setParameter("request", request);
			query.setParameter("user", user);
			query.setParameter("task", REVIEW);
			query.executeUpdate();
		} else {
			PullRequestNotification notification = new PullRequestNotification();
			notification.setRequest(request);
			notification.setUser(user);
			notification.setTask(REVIEW);
			EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
			criteria.add(Restrictions.eq("request", request))
					.add(Restrictions.eq("user", user))
					.add(Restrictions.eq("task", notification.getTask()));
			if (find(criteria) == null) {
				dao.persist(notification);
				String subject = String.format("Please review pull request #%d (%s)", 
						request.getId(), request.getTitle());
				String url = urlManager.urlFor(request);
				String body = String.format("You are invited to review pull request #%d (%s). Please visit "
						+ "<a href='%s'>%s</a> to add your comments.",
						request.getId(), request.getTitle(), url, url);
				mailManager.sendMail(Sets.newHashSet(notification.getUser()), subject, decorateMail(user, body));
			}
		}
		
	}
	
	private String decorateMail(Account user, String body) {
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

	@Transactional
	@Listen
	public void on(AccountMentioned event) {
		PullRequest request = event.getRequest();
		Account user = event.getUser();
		String subject = String.format("You are mentioned in pull request #%d (%s)", 
				request.getId(), request.getTitle());
		String url = urlManager.urlFor(request);
		String body = String.format("%s."
				+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
				+ "<p style='margin: 16px 0;'>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				subject, markdownManager.escape(request.getDescription()), url, url);
		
		mailManager.sendMail(Sets.newHashSet(user), subject, decorateMail(user, body));
	}

	@Transactional
	@Listen
	public void on(AccountMentionedInComment event) {
		PullRequestComment comment = event.getComment();
		Account user = event.getUser();
		String subject = String.format("You are mentioned in comment of pull request #%d (%s)", 
				comment.getRequest().getId(), comment.getRequest().getTitle());
		String url = urlManager.urlFor(comment);
		String body = String.format("%s."
				+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
				+ "<p style='margin: 16px 0;'>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				subject, markdownManager.escape(comment.getContent()), url, url);
		
		mailManager.sendMail(Sets.newHashSet(user), subject, decorateMail(user, body));
	}
	
	private void requestIntegration(PullRequest request) {
		Account user = request.getAssignee();
		PullRequestNotification notification = new PullRequestNotification();
		notification.setRequest(request);
		notification.setUser(user);
		notification.setTask(INTEGRATE);
		EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
		criteria.add(Restrictions.eq("request", notification.getRequest()))
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("task", notification.getTask()));
		if (find(criteria) == null) {
			dao.persist(notification);
			String subject = String.format("Please integrate pull request #%d (%s)", 
					request.getId(), request.getTitle());
			String url = urlManager.urlFor(request);
			String body = String.format("You are assignee of pull request #%d (%s). "
					+ "Please visit <a href='%s'>%s</a> to integrate it into target branch.", 
					request.getId(), request.getTitle(), url, url);
			
			mailManager.sendMail(Sets.newHashSet(notification.getUser()), subject, decorateMail(user, body));
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
			PullRequestNotification notification = new PullRequestNotification();
			notification.setRequest(request);
			notification.setUser(request.getSubmitter());
			notification.setTask(UPDATE);
			EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
			criteria.add(Restrictions.eq("request", notification.getRequest()))
					.add(Restrictions.eq("user", notification.getUser()))
					.add(Restrictions.eq("task", notification.getTask()));
			if (find(criteria) == null)
				dao.persist(notification);
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