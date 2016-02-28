package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.Notification.Task.INTEGRATE;
import static com.pmease.gitplex.core.entity.Notification.Task.REVIEW;
import static com.pmease.gitplex.core.entity.Notification.Task.UPDATE;
import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.Notification;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
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
public class DefaultNotificationManager extends AbstractEntityDao<Notification> implements NotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public DefaultNotificationManager(Dao dao, MailManager mailManager, 
			UrlManager urlManager, MarkdownManager markdownManager) {
		super(dao);
		
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.markdownManager = markdownManager;
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
		Query query = getSession().createQuery("delete from Notification "
				+ "where request=:request and task=:task");
		query.setParameter("request", update.getRequest());
		query.setParameter("task", UPDATE);
		query.executeUpdate();
	}

	@Override
	public void onCommented(Comment comment) {
	}

	@Override
	public void onCommentReplied(CommentReply reply) {
	}
	
	@Transactional
	@Override
	public void onReviewed(Review review, String comment) {
		Query query = getSession().createQuery("delete from Notification "
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
			Query query = getSession().createQuery("delete from Notification "
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
	public void onIntegrated(PullRequest request, Account user, String comment) {
		Query query = getSession().createQuery("delete from Notification "
				+ "where request=:request");
		query.setParameter("request", request);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onDiscarded(PullRequest request, Account user, String comment) {
		Query query = getSession().createQuery("delete from Notification "
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
		Account user = invitation.getReviewer();
		if (!invitation.isPreferred()) {
			Query query = getSession().createQuery("delete from Notification "
					+ "where request=:request and user=:user and task=:task");
			query.setParameter("request", request);
			query.setParameter("user", user);
			query.setParameter("task", REVIEW);
			query.executeUpdate();
		} else {
			Notification notification = new Notification();
			notification.setRequest(request);
			notification.setUser(user);
			notification.setTask(REVIEW);
			EntityCriteria<Notification> criteria = EntityCriteria.of(Notification.class);
			criteria.add(Restrictions.eq("request", request))
					.add(Restrictions.eq("user", user))
					.add(Restrictions.eq("task", notification.getTask()));
			if (find(criteria) == null) {
				persist(notification);
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
	@Override
	public void pendingApproval(PullRequest request) {
		Query query = getSession().createQuery("delete from Notification "
				+ "where request=:request and (task=:update or task=:integrate)");
		query.setParameter("request", request);
		query.setParameter("update", UPDATE);
		query.setParameter("integrate", INTEGRATE);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onMentioned(PullRequest request, Account user) {
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
	@Override
	public void onMentioned(Comment comment, Account user) {
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
	
	@Transactional
	@Override
	public void onMentioned(CommentReply reply, Account user) {
		String subject = String.format("You are mentioned in comment of pull request #%d (%s)", 
				reply.getComment().getRequest().getId(), reply.getComment().getRequest().getTitle());
		String url = urlManager.urlFor(reply);
		String body = String.format("%s."
				+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
				+ "<p style='margin: 16px 0;'>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				subject, markdownManager.escape(reply.getContent()), url, url);
		
		mailManager.sendMail(Sets.newHashSet(user), subject, decorateMail(user, body));
	}
	
	@Transactional
	@Override
	public void onReopened(PullRequest request, Account user, String comment) {
		if (request.getAssignee() != null && !request.getAssignee().equals(user))
			onAssigned(request);
	}

	private void requestIntegration(PullRequest request) {
		Account user = request.getAssignee();
		Notification notification = new Notification();
		notification.setRequest(request);
		notification.setUser(user);
		notification.setTask(INTEGRATE);
		EntityCriteria<Notification> criteria = EntityCriteria.of(Notification.class);
		criteria.add(Restrictions.eq("request", notification.getRequest()))
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("task", notification.getTask()));
		if (find(criteria) == null) {
			persist(notification);
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
		if (request.getSubmitter() != null) {
			Notification notification = new Notification();
			notification.setRequest(request);
			notification.setUser(request.getSubmitter());
			notification.setTask(UPDATE);
			EntityCriteria<Notification> criteria = EntityCriteria.of(Notification.class);
			criteria.add(Restrictions.eq("request", notification.getRequest()))
					.add(Restrictions.eq("user", notification.getUser()))
					.add(Restrictions.eq("task", notification.getTask()));
			if (find(criteria) == null)
				persist(notification);
		}
	}

}