package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestTask.Type.INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestTask.Type.REVIEW;
import static com.pmease.gitplex.core.entity.PullRequestTask.Type.UPDATE;

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
import com.pmease.gitplex.core.entity.PullRequestTask;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingApproval;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingIntegration;
import com.pmease.gitplex.core.event.pullrequest.PullRequestPendingUpdate;
import com.pmease.gitplex.core.event.pullrequest.PullRequestReviewInvitationChanged;
import com.pmease.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestUpdated;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.PullRequestTaskManager;
import com.pmease.gitplex.core.manager.UrlManager;

/**
 * This class manages pull request tasks and send task notifications if necessary. 
 * Unlike watch based notifications, task notifications can not be suppressed. 
 *   
 * @author robin
 *
 */
@Singleton
public class DefaultPullRequestTaskManager extends AbstractEntityManager<PullRequestTask> implements PullRequestTaskManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	@Inject
	public DefaultPullRequestTaskManager(Dao dao, MailManager mailManager, UrlManager urlManager) {
		super(dao);
		
		this.mailManager = mailManager;
		this.urlManager = urlManager;
	}

	@Transactional
	@Listen
	public void on(PullRequestUpdated event) {
		Query query = getSession().createQuery("delete from PullRequestTask "
				+ "where request=:request and type=:type");
		query.setParameter("request", event.getRequest());
		query.setParameter("type", UPDATE);
		query.executeUpdate();
	}

	private void onClosed(PullRequest request) {
		Query query = getSession().createQuery("delete from PullRequestTask "
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
			Query query = getSession().createQuery("delete from PullRequestTask "
					+ "where request=:request and user=:user and type=:type");
			query.setParameter("request", request);
			query.setParameter("user", user);
			query.setParameter("type", REVIEW);
			query.executeUpdate();
		} else if (invitation.getStatus() == PullRequestReviewInvitation.Status.ADDED_BY_RULE) {
			PullRequestTask task = new PullRequestTask();
			task.setRequest(request);
			task.setUser(user);
			task.setType(REVIEW);
			EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
			criteria.add(Restrictions.eq("request", request))
					.add(Restrictions.eq("user", user))
					.add(Restrictions.eq("type", task.getType()));
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
		Query query = getSession().createQuery("delete from PullRequestTask "
				+ "where request=:request and (type=:update or type=:integrate)");
		query.setParameter("request", event.getRequest());
		query.setParameter("update", UPDATE);
		query.setParameter("integrate", INTEGRATE);
		query.executeUpdate();
	}

	private void requestIntegration(PullRequest request) {
		Account user = request.getAssignee();
		PullRequestTask task = new PullRequestTask();
		task.setRequest(request);
		task.setUser(user);
		task.setType(INTEGRATE);
		EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
		criteria.add(Restrictions.eq("request", task.getRequest()))
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("type", task.getType()));
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
		PullRequestTask task = new PullRequestTask();
		task.setRequest(request);
		task.setUser(request.getSubmitter());
		task.setType(UPDATE);
		EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
		criteria.add(Restrictions.eq("request", task.getRequest()))
				.add(Restrictions.eq("user", task.getUser()))
				.add(Restrictions.eq("type", task.getType()));
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

	@Transactional
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		PullRequestStatusChange statusChange = event.getStatusChange();
		PullRequestStatusChange.Type type = statusChange.getType();
		if (type == PullRequestStatusChange.Type.APPROVED ||  type == PullRequestStatusChange.Type.DISAPPROVED) {
			onReviewed(statusChange);
		} else if (type == PullRequestStatusChange.Type.ASSIGNED) {
			PullRequest request = event.getRequest();
			Preconditions.checkNotNull(request.getAssignee());
			
			if (request.getStatus() == PENDING_INTEGRATE) {  
				Query query = getSession().createQuery("delete from PullRequestTask "
						+ "where request=:request and type=:type and user!=:user");
				query.setParameter("request", request);
				query.setParameter("type", INTEGRATE);
				query.setParameter("user", request.getAssignee());
				query.executeUpdate();
				
				requestIntegration(request);
			}
		} else if (type == PullRequestStatusChange.Type.INTEGRATED || type == PullRequestStatusChange.Type.DISCARDED) {
			onClosed(event.getRequest());
		}
	}
	
	private void onReviewed(PullRequestStatusChange statusChange) {
		Query query = getSession().createQuery("delete from PullRequestTask "
				+ "where request=:request and user=:user and type=:type");
		query.setParameter("request", statusChange.getRequest());
		query.setParameter("user", statusChange.getUser());
		query.setParameter("type", REVIEW);
		query.executeUpdate();
	}
	
}