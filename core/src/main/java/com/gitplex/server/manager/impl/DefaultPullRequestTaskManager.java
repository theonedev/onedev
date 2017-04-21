package com.gitplex.server.manager.impl;

import static com.gitplex.server.model.PullRequestTask.Type.RESOLVE_CONFLICT;
import static com.gitplex.server.model.PullRequestTask.Type.REVIEW;
import static com.gitplex.server.model.PullRequestTask.Type.UPDATE;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.ReviewInvitationChanged;
import com.gitplex.server.event.TaskChanged;
import com.gitplex.server.event.pullrequest.MergePreviewCalculated;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.event.pullrequest.PullRequestUpdated;
import com.gitplex.server.manager.MailManager;
import com.gitplex.server.manager.PullRequestTaskManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestTask;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.google.common.collect.Sets;

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
			listenerRegistry.post(new TaskChanged(task.getUser()));
			delete(task);
		}
	}

	@Transactional
	@Listen
	public void on(ReviewInvitationChanged event) {
		ReviewInvitation invitation = event.getInvitation();
		PullRequest request = invitation.getRequest();
		Account user = invitation.getUser();
		if (invitation.getType() == ReviewInvitation.Type.EXCLUDE) {
			EntityCriteria<PullRequestTask> criteria = newCriteria();
			criteria.add(Restrictions.eq("request", request)).add(Restrictions.eq("user", user)).add(Restrictions.eq("type", REVIEW));
			for (PullRequestTask task: findAll(criteria)) {
				listenerRegistry.post(new TaskChanged(task.getUser()));
				delete(task);
			}
		} else {
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
				String body = String.format("You are selected to review pull request #%d (%s).<br>"
						+ "Please visit <a href='%s'>%s</a> to do the review.",
						request.getNumber(), request.getTitle(), url, url);
				mailManager.sendMailAsync(Sets.newHashSet(task.getUser().getEmail()), subject, decorate(user, body));
				
				listenerRegistry.post(new TaskChanged(user));
			}
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
	public void on(MergePreviewCalculated event) {
		PullRequest request = event.getRequest();
		Account user = request.getSubmitter();
		if (user != null && request.getMergePreview() != null) {
			if (request.getMergePreview().getMerged() != null) {
				EntityCriteria<PullRequestTask> criteria = newCriteria();
				criteria.add(Restrictions.eq("user", user)).add(Restrictions.eq("type", RESOLVE_CONFLICT));
				for (PullRequestTask task: findAll(criteria)) {
					listenerRegistry.post(new TaskChanged(task.getUser()));
					delete(task);
				}
			} else {
				PullRequestTask task = new PullRequestTask();
				task.setRequest(request);
				task.setUser(user);
				task.setType(RESOLVE_CONFLICT);
				EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
				criteria.add(Restrictions.eq("request", task.getRequest()))
						.add(Restrictions.eq("user", user))
						.add(Restrictions.eq("type", task.getType()));
				if (find(criteria) == null) {
					save(task);
					String subject = String.format("Please resolve conflicts in pull request #%d (%s)", 
							request.getNumber(), request.getTitle());
					String url = urlManager.urlFor(request);
					String body = String.format("Pull request #%d (%s) has merge conflicts<br>"
							+ "Please visit <a href='%s'>%s</a> for details.", 
							request.getNumber(), request.getTitle(), url, url);
					
					mailManager.sendMailAsync(Sets.newHashSet(task.getUser().getEmail()), subject, decorate(user, body));
					listenerRegistry.post(new TaskChanged(user));
				}
			}
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		PullRequest request = event.getRequest();
		PullRequestStatusChange statusChange = event.getStatusChange();
		PullRequestStatusChange.Type type = statusChange.getType();
		EntityCriteria<PullRequestTask> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
		if (type == PullRequestStatusChange.Type.APPROVED || type == PullRequestStatusChange.Type.DISAPPROVED) {
			criteria.add(Restrictions.eq("user", statusChange.getUser())).add(Restrictions.eq("type", REVIEW));
			for (PullRequestTask task: findAll(criteria)) {
				listenerRegistry.post(new TaskChanged(task.getUser()));
				delete(task);
			}
			if (type == PullRequestStatusChange.Type.DISAPPROVED && request.getSubmitter() != null) {
				PullRequestTask task = new PullRequestTask();
				task.setRequest(request);
				task.setUser(request.getSubmitter());
				task.setType(UPDATE);
				criteria = EntityCriteria.of(PullRequestTask.class);
				criteria.add(Restrictions.eq("request", task.getRequest()))
						.add(Restrictions.eq("user", task.getUser()))
						.add(Restrictions.eq("type", task.getType()));
				if (find(criteria) == null) {
					save(task);
					String subject = String.format("Someone raised concerns in pull request #%d (%s)", 
							request.getNumber(), request.getTitle());
					String url = urlManager.urlFor(request);
					String body = String.format("Someone raised concerns in pull request #%d (%s).<br>"
							+ "Please visit <a href='%s'>%s</a> for details",
							request.getNumber(), request.getTitle(), url, url);
					mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter().getEmail()), subject, 
							decorate(request.getSubmitter(), body));
					listenerRegistry.post(new TaskChanged(request.getSubmitter()));
				}
			}
		} else if (type == PullRequestStatusChange.Type.MERGED || type == PullRequestStatusChange.Type.DISCARDED) {
			for (PullRequestTask task: findAll(criteria)) {
				listenerRegistry.post(new TaskChanged(task.getUser()));
				delete(task);
			}
		}
	}
	
}