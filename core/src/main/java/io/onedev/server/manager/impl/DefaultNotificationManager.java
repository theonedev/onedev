package io.onedev.server.manager.impl;

import static io.onedev.server.model.PullRequestTask.Type.RESOLVE_CONFLICT;
import static io.onedev.server.model.PullRequestTask.Type.REVIEW;
import static io.onedev.server.model.PullRequestTask.Type.UPDATE;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.util.StringUtils;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Sets;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.event.pullrequest.PullRequestStatusChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.event.pullrequest.PullRequestVerificationEvent;
import io.onedev.server.event.pullrequest.PullRequestVerificationRunning;
import io.onedev.server.event.pullrequest.PullRequestVerificationSucceeded;
import io.onedev.server.manager.BranchWatchManager;
import io.onedev.server.manager.MailManager;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.PullRequestTaskManager;
import io.onedev.server.manager.PullRequestWatchManager;
import io.onedev.server.manager.UrlManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.BranchWatch;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.model.PullRequestTask;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.ReviewInvitation;
import io.onedev.server.model.User;
import io.onedev.server.model.PullRequestStatusChange.Type;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.QualityCheckStatus;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.markdown.MentionParser;

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
			String rendered = markdownManager.render(markdown);
			Collection<User> mentionUsers = new MentionParser().parseMentions(rendered);
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
						+ "-- Sent by OneDev", 
						subject, markdownManager.escape(markdown), url, url);
				
				mailManager.sendMailAsync(mentionUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
						subject, body);
			}
		}
	}

	private boolean process(ReviewInvitation invitation, String subject) {
		PullRequest request = invitation.getRequest();
		User user = invitation.getUser();
		EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
		criteria.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("type", REVIEW));

		boolean notified = false;
		
		if (invitation.getType() == ReviewInvitation.Type.EXCLUDE) {
			for (PullRequestTask task: pullRequestTaskManager.findAll(criteria))
				pullRequestTaskManager.delete(task);
		} else {
			if (pullRequestTaskManager.find(criteria) == null) {
				PullRequestTask task = new PullRequestTask();
				task.setRequest(request);
				task.setUser(user);
				task.setType(REVIEW);
				
				pullRequestTaskManager.save(task);
				
				String url = urlManager.urlFor(request);
				String body = String.format(""
						+ "Dear %s,"
						+ "<p style='margin: 16px 0;'>"
						+ "%s"
						+ "<p style='margin: 16px 0;'>"
						+ "Visit <a href='%s'>%s</a> for details."
						+ "<p style='margin: 16px 0;'>"
						+ "-- Sent by OneDev",
						user.getDisplayName(), subject, url, url);
				mailManager.sendMailAsync(Sets.newHashSet(task.getUser().getEmail()), subject, body);
				notified = true;
			}
			watch(request, user, "You are set to watch this pull request as you are invited as a reviewer.");
		}
		return notified;
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
			String subject = String.format("You are invited to review pull request #%d - %s", request.getNumber(), 
					request.getTitle());
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (process(invitation, subject))
					notifiedUsers.add(invitation.getUser());
			}
		} else if (event instanceof PullRequestUpdated) {
			EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
			criteria.add(Restrictions.eq("request", request)).add(Restrictions.eq("type", UPDATE));
			for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) 
				pullRequestTaskManager.delete(task);
			
			if (!request.isMergeIntoTarget()) {
				QualityCheckStatus qualityCheckStatus = request.getQualityCheckStatus();					
				for (ReviewInvitation invitation: request.getReviewInvitations()) {
					String subject = String.format("There are new changes to review in pull request #%d - %s", 
							request.getNumber(), request.getTitle());
					if (qualityCheckStatus.getAwaitingReviewers().contains(invitation.getUser())) { 
						if (process(invitation, subject))
							notifiedUsers.add(invitation.getUser());
					}
				}
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
								+ "-- Sent by OneDev", 
								request.getSubmitter().getDisplayName(), request.getNumber(), request.getTitle(), 
								url, url);
						
						mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter().getEmail()), subject, body); 
						
						notifiedUsers.add(request.getSubmitter());
					}
				}
			}
		} else if (event instanceof PullRequestStatusChangeEvent) {
			PullRequestStatusChangeEvent statusChangeEvent = (PullRequestStatusChangeEvent)event;
			PullRequestStatusChange statusChange = statusChangeEvent.getStatusChange();
			PullRequestStatusChange.Type type = statusChange.getType();
			EntityCriteria<PullRequestTask> criteria = EntityCriteria.of(PullRequestTask.class);
			criteria.add(Restrictions.eq("request", request));
			if (type == PullRequestStatusChange.Type.APPROVED || type == PullRequestStatusChange.Type.DISAPPROVED) {
				criteria.add(Restrictions.eq("user", statusChange.getUser())).add(Restrictions.eq("type", REVIEW));
				for (PullRequestTask task: pullRequestTaskManager.findAll(criteria)) 
					pullRequestTaskManager.delete(task);
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
								+ "-- Sent by OneDev",
								request.getSubmitter().getDisplayName(), event.getUser().getDisplayName(), 
								request.getNumber(), request.getTitle(), url, url);
						mailManager.sendMailAsync(Sets.newHashSet(request.getSubmitter().getEmail()), subject, body);
						notifiedUsers.add(request.getSubmitter());
					}
				}
			} else if (type == PullRequestStatusChange.Type.MERGED || type == PullRequestStatusChange.Type.DISCARDED) {
				for (PullRequestTask task: pullRequestTaskManager.findAll(criteria))
					pullRequestTaskManager.delete(task);
			} else if (type == PullRequestStatusChange.Type.REMOVED_REVIEWER) {
				String subject = String.format("You no longer needs to review pull request #%d - %s", request.getNumber(), 
						request.getTitle());
				ReviewInvitation invitation = (ReviewInvitation) statusChangeEvent.getStatusData();
				if (process(invitation, subject))
					notifiedUsers.add(invitation.getUser());
			} else if (type == PullRequestStatusChange.Type.ADDED_REVIEWER) {
				String subject = String.format("You are invited to review pull request #%d - %s", request.getNumber(), 
						request.getTitle());
				ReviewInvitation invitation = (ReviewInvitation) statusChangeEvent.getStatusData();
				if (process(invitation, subject))
					notifiedUsers.add(invitation.getUser());
			}
		}
		
		// handle mentions
		Set<User> mentionUsers = new HashSet<>();
		if (event instanceof MarkdownAware && (!(event instanceof PullRequestCodeCommentEvent) || !((PullRequestCodeCommentEvent)event).isPassive())) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			if (markdown != null) {
				String rendered = markdownManager.render(markdown);
				mentionUsers.addAll(new MentionParser().parseMentions(rendered));
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
							+ "-- Sent by OneDev", 
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
				&& !(event instanceof PullRequestVerificationRunning)
				&& !(event instanceof PullRequestVerificationSucceeded)) {
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
							+ "<p style='margin: 16px 0;'>-- Sent by OneDev"
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
						activity = "OneDev " + activity;
					}
					
					subject = String.format("%s pull request #%d - %s", activity, request.getNumber(), request.getTitle()); 
					body = String.format("Dear Users,"
							+ "<p style='margin: 16px 0;'>%s pull request #%d - %s "
							+ "<p style='margin: 16px 0;'>Visit <a href='%s'>%s</a> for details."
							+ "<p style='margin: 16px 0;'>-- Sent by OneDev"
							+ "<p style='margin: 16px 0; font-size: 12px; color: #888;'>"
							+ "You receive this email as you are watching the pull request.",
							activity, request.getNumber(), request.getTitle(), url, url);
				}
				mailManager.sendMailAsync(usersToNotify.stream().map(User::getEmail).collect(Collectors.toList()), subject, body);
			}
		}
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

}
