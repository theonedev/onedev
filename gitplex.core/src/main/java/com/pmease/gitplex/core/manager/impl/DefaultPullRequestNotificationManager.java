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
import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.JsoupUtils;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestNotificationManager;
import com.pmease.gitplex.core.manager.UrlManager;
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
	
	private final UrlManager urlManager;
	
	@Inject
	public DefaultPullRequestNotificationManager(Dao dao, MailManager mailManager, UrlManager urlManager) {
		this.dao = dao;
		this.mailManager = mailManager;
		this.urlManager = urlManager;
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
					+ "where request=:request and task=:task and user!=:user");
			query.setParameter("request", request);
			query.setParameter("task", INTEGRATE);
			query.setParameter("user", request.getAssignee());
			query.executeUpdate();
			
			requestIntegration(request);
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
	}

	@Transactional
	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
		PullRequest request = invitation.getRequest();
		User user = invitation.getReviewer();
		if (!invitation.isPreferred()) {
			Query query = dao.getSession().createQuery("delete from PullRequestNotification "
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
			if (dao.find(criteria) == null) {
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
	
	private String decorateMail(User user, String body) {
		return String.format("Dear %s, "
				+ "<p>"
				+ "%s"
				+ "<p>"
				+ "-- Sent by GitPlex", 
				user.getDisplayName(), body);
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
	public void onMentioned(PullRequest request, User user) {
		String subject = String.format("You are mentioned in pull request #%d (%s)", 
				request.getId(), request.getTitle());
		String url = urlManager.urlFor(request);
		String body = String.format("<pre>%s</pre>"
				+ "<p>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				JsoupUtils.sanitize(request.getDescription()), url, url);
		
		mailManager.sendMail(Sets.newHashSet(user), subject, decorateMail(user, body));
	}

	@Transactional
	@Override
	public void onMentioned(PullRequestComment comment, User user) {
		String subject = String.format("You are mentioned in comment of pull request #%d (%s)", 
				comment.getRequest().getId(), comment.getRequest().getTitle());
		String url = urlManager.urlFor(comment);
		String body = String.format("<pre>%s</pre>"
				+ "<p>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				JsoupUtils.sanitize(comment.getContent()), url, url);
		
		mailManager.sendMail(Sets.newHashSet(user), subject, decorateMail(user, body));
	}
	
	@Transactional
	@Override
	public void onMentioned(PullRequestCommentReply reply, User user) {
		String subject = String.format("You are mentioned in comment of pull request #%d (%s)", 
				reply.getComment().getRequest().getId(), reply.getComment().getRequest().getTitle());
		String url = urlManager.urlFor(reply);
		String body = String.format("<pre>%s</pre>"
				+ "<p>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				JsoupUtils.sanitize(reply.getContent()), url, url);
		
		mailManager.sendMail(Sets.newHashSet(user), subject, decorateMail(user, body));
	}
	
	@Transactional
	@Override
	public void onReopened(PullRequest request, User user, String comment) {
		if (request.getAssignee() != null && !request.getAssignee().equals(user))
			onAssigned(request);
	}

	private void requestIntegration(PullRequest request) {
		User user = request.getAssignee();
		PullRequestNotification notification = new PullRequestNotification();
		notification.setRequest(request);
		notification.setUser(user);
		notification.setTask(INTEGRATE);
		EntityCriteria<PullRequestNotification> criteria = EntityCriteria.of(PullRequestNotification.class);
		criteria.add(Restrictions.eq("request", notification.getRequest()))
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("task", notification.getTask()));
		if (dao.find(criteria) == null) {
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
	@Override
	public void pendingIntegration(PullRequest request) {
		if (request.getAssignee() != null) 
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