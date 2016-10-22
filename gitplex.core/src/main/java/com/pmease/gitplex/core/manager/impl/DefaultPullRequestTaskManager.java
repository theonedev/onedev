package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestTask.Type.INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequestTask.Type.REVIEW;
import static com.pmease.gitplex.core.entity.PullRequestTask.Type.UPDATE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestTask;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.event.TaskChangeEvent;
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
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestTaskManager(Dao dao, MailManager mailManager, UrlManager urlManager, ListenerRegistry listenerRegistry) {
		super(dao);
		
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Listen
	public void on(PullRequestUpdated event) {
		EntityCriteria<PullRequestTask> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", event.getRequest())).add(Restrictions.eq("type", UPDATE));
		for (PullRequestTask task: findAll(criteria)) {
			listenerRegistry.post(new TaskChangeEvent(task.getUser()));
			delete(task);
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestReviewInvitationChanged event) {
		PullRequestReviewInvitation invitation = event.getInvitation();
		PullRequest request = invitation.getRequest();
		Account user = invitation.getUser();
		if (invitation.getStatus() == PullRequestReviewInvitation.Status.EXCLUDED) {
			EntityCriteria<PullRequestTask> criteria = newCriteria();
			criteria.add(Restrictions.eq("request", request)).add(Restrictions.eq("user", user)).add(Restrictions.eq("type", REVIEW));
			for (PullRequestTask task: findAll(criteria)) {
				listenerRegistry.post(new TaskChangeEvent(task.getUser()));
				delete(task);
			}
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
				
				listenerRegistry.post(new TaskChangeEvent(user));
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
		EntityCriteria<PullRequestTask> criteria = newCriteria();
		Criterion typeCriterion = Restrictions.or(Restrictions.eq("type", UPDATE), Restrictions.eq("type", INTEGRATE));
		criteria.add(Restrictions.eq("request", event.getRequest())).add(typeCriterion);
		for (PullRequestTask task: findAll(criteria)) {
			listenerRegistry.post(new TaskChangeEvent(task.getUser()));
			delete(task);
		}
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
			listenerRegistry.post(new TaskChangeEvent(user));
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
			listenerRegistry.post(new TaskChangeEvent(request.getSubmitter()));
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		PullRequestStatusChange statusChange = event.getStatusChange();
		PullRequestStatusChange.Type type = statusChange.getType();
		EntityCriteria<PullRequestTask> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", event.getRequest()));
		if (type == PullRequestStatusChange.Type.APPROVED ||  type == PullRequestStatusChange.Type.DISAPPROVED) {
			criteria.add(Restrictions.eq("user", statusChange.getUser())).add(Restrictions.eq("type", REVIEW));
			for (PullRequestTask task: findAll(criteria)) {
				listenerRegistry.post(new TaskChangeEvent(task.getUser()));
				delete(task);
			}
		} else if (type == PullRequestStatusChange.Type.ASSIGNED) {
			PullRequest request = event.getRequest();
			Preconditions.checkNotNull(request.getAssignee());
			
			if (request.getStatus() == PENDING_INTEGRATE) {  
				criteria.add(Restrictions.ne("user", request.getAssignee())).add(Restrictions.eq("type", INTEGRATE));
				for (PullRequestTask task: findAll(criteria)) {
					listenerRegistry.post(new TaskChangeEvent(task.getUser()));
					delete(task);
				}
				requestIntegration(request);
			}
		} else if (type == PullRequestStatusChange.Type.INTEGRATED || type == PullRequestStatusChange.Type.DISCARDED) {
			for (PullRequestTask task: findAll(criteria)) {
				listenerRegistry.post(new TaskChangeEvent(task.getUser()));
				delete(task);
			}
		}
	}
	
}