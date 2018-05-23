package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.util.StringUtils;

import com.google.common.collect.Sets;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentReplied;
import io.onedev.server.event.pullrequest.PullRequestCodeCommented;
import io.onedev.server.event.pullrequest.PullRequestCommented;
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
import io.onedev.server.manager.PullRequestNotificationManager;
import io.onedev.server.manager.PullRequestWatchManager;
import io.onedev.server.manager.TaskManager;
import io.onedev.server.manager.UrlManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.BranchWatch;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.model.PullRequestStatusChange.Type;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.ReviewInvitation;
import io.onedev.server.model.Task;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.QualityCheckStatus;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.markdown.MentionParser;

@Singleton
public class DefaultPullRequestNotificationManager implements PullRequestNotificationManager {
	
	private final BranchWatchManager branchWatchManager;
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final VisitManager visitInfoManager;
	
	private final MarkdownManager markdownManager;
	
	private final TaskManager taskManager;
	
	private final PullRequestWatchManager pullRequestWatchManager;
	
	@Inject
	public DefaultPullRequestNotificationManager(MarkdownManager markdownManager, BranchWatchManager branchWatchManager, 
			MailManager mailManager, UrlManager urlManager, VisitManager visitInfoManager, 
			PullRequestWatchManager pullRequestWatchManager, TaskManager taskManager) {
		this.branchWatchManager = branchWatchManager;
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.visitInfoManager = visitInfoManager;
		this.markdownManager = markdownManager;
		this.taskManager = taskManager;
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

		boolean notified = false;
		
		if (invitation.getType() == ReviewInvitation.Type.EXCLUDE) {
			taskManager.deleteTasks(request, user, Task.DESC_REVIEW);
		} else {
			if (taskManager.findAll(request, user, Task.DESC_REVIEW).isEmpty()) {
				taskManager.addTask(request, user, Task.DESC_REVIEW);
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
				mailManager.sendMailAsync(Sets.newHashSet(user.getEmail()), subject, body);
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
			taskManager.deleteTasks(request, null, Task.DESC_UPDATE);
			
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
					taskManager.deleteTasks(request, request.getSubmitter(), Task.DESC_RESOLVE_CONFLICT);
				} else {
					if (taskManager.findAll(request, request.getSubmitter(), Task.DESC_RESOLVE_CONFLICT).isEmpty()) {
						taskManager.addTask(request, request.getSubmitter(), Task.DESC_RESOLVE_CONFLICT);
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
			if (type == PullRequestStatusChange.Type.APPROVED || type == PullRequestStatusChange.Type.DISAPPROVED) {
				taskManager.deleteTasks(request, statusChange.getUser(), Task.DESC_REVIEW);
				if (type == PullRequestStatusChange.Type.DISAPPROVED && request.getSubmitter() != null) {
					if (taskManager.findAll(request, request.getSubmitter(), Task.DESC_UPDATE).isEmpty()) {
						taskManager.addTask(request, request.getSubmitter(), Task.DESC_UPDATE);
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
				taskManager.deleteTasks(request, null, null);
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
					if (event instanceof PullRequestCommented)
						url = urlManager.urlFor(((PullRequestCommented)event).getComment());
					else if (event instanceof PullRequestStatusChangeEvent) 
						url = urlManager.urlFor(((PullRequestStatusChangeEvent)event).getStatusChange());
					else if (event instanceof PullRequestCodeCommented)
						url = urlManager.urlFor(((PullRequestCodeCommented)event).getComment(), request);
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
			eventType = EditableUtils.getDisplayName(event.getClass());
			
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
					if (request.getLastActivity() == null) {
						usersToNotify.add(watch.getUser());
					} else {
						Date visitDate = visitInfoManager.getPullRequestVisitDate(watch.getUser(), request);
						if (visitDate == null || visitDate.getTime()<request.getLastActivity().getDate().getTime()) {
							if (!request.getLastActivity().getAction().equals(eventType)) { 
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
