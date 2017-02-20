package com.gitplex.server.manager.impl;

import static com.gitplex.server.model.PullRequest.Status.PENDING_INTEGRATE;
import static com.gitplex.server.model.PullRequestTask.Type.INTEGRATE;
import static com.gitplex.server.model.PullRequestTask.Type.REVIEW;
import static com.gitplex.server.model.PullRequestTask.Type.UPDATE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.TaskChangeEvent;
import com.gitplex.server.event.pullrequest.PullRequestPendingApproval;
import com.gitplex.server.event.pullrequest.PullRequestPendingIntegration;
import com.gitplex.server.event.pullrequest.PullRequestPendingUpdate;
import com.gitplex.server.event.pullrequest.PullRequestReviewInvitationChanged;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.event.pullrequest.PullRequestUpdated;
import com.gitplex.server.manager.MailManager;
import com.gitplex.server.manager.PullRequestTaskManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestReviewInvitation;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestTask;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

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
				mailManager.sendMailAsync(Sets.newHashSet(task.getUser().getEmail()), subject, decorate(user, body));
				
				listenerRegistry.post(new TaskChangeEvent(user));
			}
		} else {
			String subject = String.format("You are invited to review pull request #%d (%s)", 
					request.getNumber(), request.getTitle());
			String url = urlManager.urlFor(request);
			String body = String.format("You are invited to review pull request #%d (%s).<br>"
					+ "Please visit <a href='%s'>%s</a> to do the review.",
					request.getNumber(), request.getTitle(), url, url);
			mailManager.sendMailAsync(Sets.newHashSet(invitation.getUser().getEmail()), subject, decorate(user, body));
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
			
			mailManager.sendMailAsync(Sets.newHashSet(task.getUser().getEmail()), subject, decorate(user, body));
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
			mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter().getEmail()), subject, 
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