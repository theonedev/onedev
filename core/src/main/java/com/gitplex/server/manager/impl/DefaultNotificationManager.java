package com.gitplex.server.manager.impl;

import static com.gitplex.server.model.PullRequestTask.Type.RESOLVE_CONFLICT;
import static com.gitplex.server.model.PullRequestTask.Type.REVIEW;
import static com.gitplex.server.model.PullRequestTask.Type.UPDATE;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.util.StringUtils;
import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.event.codecomment.CodeCommentCreated;
import com.gitplex.server.event.codecomment.CodeCommentEvent;
import com.gitplex.server.event.codecomment.CodeCommentReplied;
import com.gitplex.server.event.pullrequest.PullRequestMergePreviewCalculated;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentReplied;
import com.gitplex.server.event.pullrequest.PullRequestCommentCreated;
import com.gitplex.server.event.pullrequest.PullRequestEvent;
import com.gitplex.server.event.pullrequest.PullRequestOpened;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.event.pullrequest.PullRequestUpdated;
import com.gitplex.server.event.pullrequest.PullRequestVerificationEvent;
import com.gitplex.server.event.pullrequest.PullRequestVerificationRunning;
import com.gitplex.server.manager.BranchWatchManager;
import com.gitplex.server.manager.MailManager;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.manager.PullRequestTaskManager;
import com.gitplex.server.manager.PullRequestWatchManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.manager.VisitManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.BranchWatch;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestStatusChange.Type;
import com.gitplex.server.model.PullRequestTask;
import com.gitplex.server.model.PullRequestWatch;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.persistence.dao.EntityPersisted;
import com.gitplex.server.util.editable.EditableUtils;
import com.gitplex.server.util.markdown.MentionParser;
import com.google.common.collect.Sets;

@Singleton
public class DefaultNotificationManager implements NotificationManager {
	
	private final BranchWatchManager branchWatchManager;
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final VisitManager visitInfoManager;
	
	private final MarkdownManager markdownManager;
	
	private final PullRequestTaskManager pullRequestTaskManager;
	
	private final PullRequestWatchManager pullRequestWatchManager;
	
	@Inject
	public DefaultNotificationManager(MarkdownManager markdownManager, BranchWatchManager branchWatchManager, 
			MailManager mailManager, UrlManager urlManager, VisitManager visitInfoManager, 
			PullRequestWatchManager pullRequestWatchManager, PullRequestTaskManager pullRequestTaskManager) {
		this.branchWatchManager = branchWatchManager;
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.visitInfoManager = visitInfoManager;
		this.markdownManager = markdownManager;
		this.pullRequestTaskManager = pullRequestTaskManager;
		this.pullRequestWatchManager = pullRequestWatchManager;
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getRequest() == null) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			String html = markdownManager.render(markdown, null, false);
			Collection<User> mentionUsers = new MentionParser().parseMentions(html);
			if (!mentionUsers.isEmpty()) {
				String url;
				if (event instanceof CodeCommentCreated)
					url = urlManager.urlFor(((CodeCommentCreated)event).getComment(), null);
				else 
					url = urlManager.urlFor(((CodeCommentReplied)event).getReply(), null);
				
				String subject = String.format("You are mentioned in a code comment on file '%s'", 
						event.getComment().getMarkPos().getPath());
				String body = String.format(""
						+ "Dear Users,"
						+ "<p style='margin: 16px 0;'>"
						+ "%s."
						+ "<p style='margin: 16px 0;'>"
						+ "<div style='padding-left: 16px; border-left: 4px solid #CCC;'>%s</div>"
						+ "<p style='margin: 16px 0;'>"
						+ "Visit <a href='%s'>%s</a> for details."
						+ "<p style='margin: 16px 0;'>"
						+ "-- Sent by GitPlex", 
						subject, markdownManager.escape(markdown), url, url);
				
				mailManager.sendMailAsync(mentionUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
						subject, body);
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		PullRequest request = event.getRequest();

		Set<User> notifiedUsers = new HashSet<>();

		// Update pull request tasks
		if (event instanceof PullRequestOpened) {
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (invitation.getType() != ReviewInvitation.Type.EXCLUDE) 
					notifiedUsers.add(invitation.getUser());
			}
		} else if (event instanceof PullRequestUpdated) {
			EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
			criteria.add(Restrictions.eq("request", request)).add(Restrictions.eq("type", UPDATE));
			for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) {
				pullRequestTaskManager.delete(task);
			}
		} else if (event instanceof PullRequestMergePreviewCalculated) {
			if (request.getSubmitter() != null && request.getMergePreview() != null) {
				if (request.getMergePreview().getMerged() != null) {
					EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
					criteria.add(Restrictions.eq("user", request.getSubmitter()))
							.add(Restrictions.eq("type", RESOLVE_CONFLICT));
					for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) {
						pullRequestTaskManager.delete(task);
					}
				} else {
					PullRequestTask task = new PullRequestTask();
					task.setRequest(request);
					task.setUser(request.getSubmitter());
					task.setType(RESOLVE_CONFLICT);
					EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
					criteria.add(Restrictions.eq("request", task.getRequest()))
							.add(Restrictions.eq("user", request.getSubmitter()))
							.add(Restrictions.eq("type", task.getType()));
					if (pullRequestTaskManager.find(criteria) == null) {
						pullRequestTaskManager.save(task);
						String subject = String.format("Please resolve conflicts in pull request #%d - %s", 
								request.getNumber(), request.getTitle());
						String url = urlManager.urlFor(request);
						String body = String.format(""
								+ "Dear %s,"
								+ "<p style='margin: 16px 0;'>"
								+ "There are merge conficts in pull request #%d - %s."
								+ "<p style='margin: 16px 0;'>"
								+ "Visit <a href='%s'>%s</a> for details."
								+ "<p style='margin: 16px 0;'>"
								+ "-- Sent by GitPlex", 
								request.getSubmitter().getDisplayName(), request.getNumber(), request.getTitle(), 
								url, url);
						
						mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter().getEmail()), subject, body); 
						
						notifiedUsers.add(request.getSubmitter());
					}
				}
			}
		} else if (event instanceof PullRequestStatusChangeEvent) {
			PullRequestStatusChange statusChange = ((PullRequestStatusChangeEvent)event).getStatusChange();
			PullRequestStatusChange.Type type = statusChange.getType();
			EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
			criteria.add(Restrictions.eq("request", request));
			if (type == PullRequestStatusChange.Type.APPROVED || type == PullRequestStatusChange.Type.DISAPPROVED) {
				criteria.add(Restrictions.eq("user", statusChange.getUser())).add(Restrictions.eq("type", REVIEW));
				for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) {
					pullRequestTaskManager.delete(task);
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
					if (pullRequestTaskManager.find(criteria) == null) {
						pullRequestTaskManager.save(task);
						String subject = String.format("%s disapproved pull request #%d - %s", 
								event.getUser().getDisplayName(), request.getNumber(), request.getTitle());
						String url = urlManager.urlFor(request);
						String body = String.format(""
								+ "Dear %s,"
								+ "<p style='margin: 16px 0;'>"
								+ "%s disapproved pull request #%d - %s."
								+ "<p style='margin: 16px 0;'>"
								+ "Visit <a href='%s'>%s</a> for details."
								+ "<p style='margin: 16px 0;'>"
								+ "-- Sent by GitPlex",
								request.getSubmitter().getDisplayName(), event.getUser().getDisplayName(), 
								request.getNumber(), request.getTitle(), url, url);
						mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter().getEmail()), subject, body);
						notifiedUsers.add(request.getSubmitter());
					}
				}
			} else if (type == PullRequestStatusChange.Type.MERGED || type == PullRequestStatusChange.Type.DISCARDED) {
				for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) {
					pullRequestTaskManager.delete(task);
				}
			}
		}
		
		// handle mentions
		Set<User> mentionUsers = new HashSet<>();
		if (event instanceof MarkdownAware && (!(event instanceof PullRequestCodeCommentEvent) || !((PullRequestCodeCommentEvent)event).isPassive())) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			if (markdown != null) {
				String html = markdownManager.render(markdown, null, false);
				mentionUsers.addAll(new MentionParser().parseMentions(html));
				if (!mentionUsers.isEmpty()) {
					String url;
					if (event instanceof PullRequestCommentCreated)
						url = urlManager.urlFor(((PullRequestCommentCreated)event).getComment());
					else if (event instanceof PullRequestStatusChangeEvent) 
						url = urlManager.urlFor(((PullRequestStatusChangeEvent)event).getStatusChange());
					else if (event instanceof PullRequestCodeCommentCreated)
						url = urlManager.urlFor(((PullRequestCodeCommentCreated)event).getComment(), request);
					else if (event instanceof PullRequestCodeCommentReplied)
						url = urlManager.urlFor(((PullRequestCodeCommentReplied)event).getReply(), request);
					else 
						url = urlManager.urlFor(event.getRequest());
					
					String subject = String.format("You are mentioned in pull request #%d - %s", 
							request.getNumber(), request.getTitle());
					String body = String.format(""
							+ "Dear Users,"
							+ "<p style='margin: 16px 0;'>"
							+ "%s."
							+ "<p style='margin: 16px 0;'>"
							+ "<div style='padding-left: 16px; border-left: 4px solid #CCC;'>%s</div>"
							+ "<p style='margin: 16px 0;'>"
							+ "Visit <a href='%s'>%s</a> for details."
							+ "<p style='margin: 16px 0;'>"
							+ "-- Sent by GitPlex", 
							subject, markdownManager.escape(markdown), url, url);
					
					mailManager.sendMailAsync(mentionUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
							subject, body);
				}
			}
		} 		
		
		notifiedUsers.addAll(mentionUsers);
		
		// Update watch list
		String eventType;
		if (event instanceof PullRequestStatusChangeEvent)
			eventType = ((PullRequestStatusChangeEvent) event).getStatusChange().getType().getName();
		else
			eventType = EditableUtils.getName(event.getClass());
			
		String activity = eventType;
		if (activity.contains(" "))
			activity += " in";
		
		if (event.getUser() != null) 
			watch(request, event.getUser(), "You've set to watch this pull request as you've " + activity + " it");
		
		if (event instanceof PullRequestOpened) {
			for (BranchWatch branchWatch: 
					branchWatchManager.find(request.getTargetProject(), request.getTargetBranch())) {
				watch(request, branchWatch.getUser(), 
						"You are set to watch this pull request as you are watching the target branch.");
			}
		} 
		
		for (User mention: mentionUsers) {
			watch(request, mention, 
					"You are set to watch this pull request as you are mentioned in code comment.");
		}
		
		// notify watchers
		boolean notifyWatchers = false;
		if (event instanceof PullRequestStatusChangeEvent) {
			PullRequestStatusChange.Type type = ((PullRequestStatusChangeEvent) event).getStatusChange().getType();
			if (type == Type.APPROVED || type == Type.DISAPPROVED || type == Type.MERGED || type == Type.REOPENED
					|| type == Type.DISCARDED || type == Type.WITHDRAWED_REVIEW) {
				notifyWatchers = true;
			}
		} else if (!(event instanceof PullRequestMergePreviewCalculated) 
				&& !(event instanceof PullRequestVerificationRunning)) {
			notifyWatchers = true;
		}
		
		if (notifyWatchers) {
			Collection<User> usersToNotify = new HashSet<>();
			
			for (PullRequestWatch watch: request.getWatches()) {
				if (!watch.isIgnore() && !watch.getUser().equals(event.getUser()) 
						&& (!(event instanceof PullRequestUpdated) 
								|| !watch.getUser().equals(request.getSubmitter()))) { 
					if (request.getLastEvent() == null) {
						usersToNotify.add(watch.getUser());
					} else {
						Date visitDate = visitInfoManager.getPullRequestVisitDate(watch.getUser(), request);
						if (visitDate == null || visitDate.getTime()<request.getLastEvent().getDate().getTime()) {
							if (!request.getLastEvent().getType().equals(eventType)) { 
								usersToNotify.add(watch.getUser());
							} 
						} else {
							usersToNotify.add(watch.getUser());
						}
					}
				}
			}

			usersToNotify.removeAll(notifiedUsers);
			
			if (!usersToNotify.isEmpty()) {
				String subject, body;
				String url = urlManager.urlFor(request);
				if (event instanceof PullRequestOpened) {
					subject = String.format("%s opened pull request #%d - %s", 
							request.getSubmitter().getDisplayName(), request.getNumber(), request.getTitle()); 
					body = String.format("Dear Users,"
							+ "<p style='margin: 16px 0;'>%s opened pull request #%d - %s"
							+ "<p style='margin: 16px 0;'>Visit <a href='%s'>%s</a> for details."
							+ "<p style='margin: 16px 0;'>-- Sent by GitPlex"
							+ "<p style='margin: 16px 0; font-size: 12px; color: #888;'>"
							+ "You receive this email as you are watching target branch.",
							request.getSubmitter().getDisplayName(), request.getNumber(), request.getTitle(), url, url);
				} else {
					if (event.getUser() != null) {
						activity = event.getUser().getDisplayName() + " " + activity;
					} else if (event instanceof PullRequestUpdated 
							|| event instanceof PullRequestMergePreviewCalculated 
							|| event instanceof PullRequestVerificationEvent) {
						activity = StringUtils.capitalize(activity);
					} else {
						activity = "GitPlex " + activity;
					}
					
					subject = String.format("%s pull request #%d - %s", activity, request.getNumber(), request.getTitle()); 
					body = String.format("Dear Users,"
							+ "<p style='margin: 16px 0;'>%s pull request #%d - %s "
							+ "<p style='margin: 16px 0;'>Visit <a href='%s'>%s</a> for details."
							+ "<p style='margin: 16px 0;'>-- Sent by GitPlex"
							+ "<p style='margin: 16px 0; font-size: 12px; color: #888;'>"
							+ "You receive this email as you are watching the pull request.",
							activity, request.getNumber(), request.getTitle(), url, url);
				}
				mailManager.sendMailAsync(usersToNotify.stream().map(User::getEmail).collect(Collectors.toList()), subject, body);
			}
		}
	}
	
	private void requestReview(ReviewInvitation invitation) {
		PullRequest request = invitation.getRequest();
		
		PullRequestTask task = new PullRequestTask();
		task.setRequest(request);
		task.setUser(invitation.getUser());
		task.setType(REVIEW);
		
		pullRequestTaskManager.save(task);
		
		String subject = String.format("Please review pull request #%d - %s", request.getNumber(), request.getTitle());
		String url = urlManager.urlFor(request);
		String body = String.format(""
				+ "Dear %s,"
				+ "<p style='margin: 16px 0;'>"
				+ "You are selected to review pull request #%d - %s."
				+ "<p style='margin: 16px 0;'>"
				+ "Visit <a href='%s'>%s</a> for details."
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex",
				invitation.getUser().getDisplayName(), request.getNumber(), request.getTitle(), url, url);
		mailManager.sendMailAsync(Sets.newHashSet(task.getUser().getEmail()), subject, body);
	}
	
	private void watch(PullRequest request, User user, String reason) {
		PullRequestWatch watch = request.getWatch(user);
		if (watch == null) {
			watch = new PullRequestWatch();
			watch.setRequest(request);
			watch.setUser(user);
			watch.setReason(reason);
			request.getWatches().add(watch);
			pullRequestWatchManager.save(watch);
		}
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof ReviewInvitation) {
			ReviewInvitation invitation = (ReviewInvitation) event.getEntity();
			PullRequest request = invitation.getRequest();
			User user = invitation.getUser();
			if (invitation.getType() == ReviewInvitation.Type.EXCLUDE) {
				EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
				criteria.add(Restrictions.eq("request", request)).add(Restrictions.eq("user", user)).add(Restrictions.eq("type", REVIEW));
				for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) {
					pullRequestTaskManager.delete(task);
				}
			} else {
				EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
				criteria.add(Restrictions.eq("request", request))
						.add(Restrictions.eq("user", user))
						.add(Restrictions.eq("type", PullRequestTask.Type.REVIEW));
				if (pullRequestTaskManager.find(criteria) == null)
					requestReview(invitation);
				watch(invitation.getRequest(), invitation.getUser(), 
						"You are set to watch this pull request as you are invited as a reviewer.");
			} 
		}
	}

}
