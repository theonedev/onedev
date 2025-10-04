package io.onedev.server.notification;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.server.service.PullRequestMentionService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.pullrequest.*;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.*;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.xodus.VisitInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.shiro.authz.Permission;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isNotified;

@Singleton
public class PullRequestNotificationManager {

	@Inject
	private MailService mailService;

	@Inject
	private PullRequestWatchService watchService;

	@Inject
	private VisitInfoService userInfoManager;

	@Inject
	private UserService userService;

	@Inject
	private PullRequestMentionService mentionService;

	@Inject
	private SettingService settingService;

	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (event.getUser() == null || !event.getUser().isServiceAccount()) {
			PullRequest request = event.getRequest();
			User user = event.getUser();
	
			String url = event.getUrl();
	
			for (Map.Entry<User, Boolean> entry : new QueryWatchBuilder<PullRequest>() {
	
				@Override
				protected PullRequest getEntity() {
					return request;
				}
	
				@Override
				protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
					return request.getTargetProject().getPullRequestQueryPersonalizations();
				}
	
				@Override
				protected EntityQuery<PullRequest> parse(String queryString) {
					return PullRequestQuery.parse(request.getTargetProject(), queryString, true);
				}
	
				@Override
				protected Collection<? extends NamedQuery> getNamedQueries() {
					return request.getTargetProject().getNamedPullRequestQueries();
				}
	
			}.getWatches().entrySet()) {
				watchService.watch(request, entry.getKey(), entry.getValue());
			}
	
			for (Map.Entry<User, Boolean> entry : new QueryWatchBuilder<PullRequest>() {
	
				@Override
				protected PullRequest getEntity() {
					return request;
				}
	
				@Override
				protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
					return userService.query().stream().map(it -> it.getPullRequestQueryPersonalization()).collect(Collectors.toList());
				}
	
				@Override
				protected EntityQuery<PullRequest> parse(String queryString) {
					return PullRequestQuery.parse(null, queryString, true);
				}
	
				@Override
				protected Collection<? extends NamedQuery> getNamedQueries() {
					return settingService.getPullRequestSetting().getNamedQueries();
				}
	
			}.getWatches().entrySet()) {
				watchService.watch(request, entry.getKey(), entry.getValue());
			}
	
			Collection<String> notifiedEmailAddresses;
			if (event instanceof PullRequestCommentCreated)
				notifiedEmailAddresses = ((PullRequestCommentCreated) event).getNotifiedEmailAddresses();
			else
				notifiedEmailAddresses = new HashSet<>();
			
			Collection<User> notifiedUsers = Sets.newHashSet();
			if (user != null) {
				if (!user.isNotifyOwnEvents() || isNotified(notifiedEmailAddresses, user))
					notifiedUsers.add(user); 
				if (!user.isSystem() && !user.isServiceAccount())
					watchService.watch(request, user, true);
			}
	
			User committer = null;
			if (event instanceof PullRequestUpdated) {
				PullRequestUpdated pullRequestUpdated = (PullRequestUpdated) event;
				Collection<User> committers = pullRequestUpdated.getCommitters();
				if (committers.size() == 1) {
					committer = committers.iterator().next();
					notifiedUsers.add(committer);
				}
				for (User each : committers) {
					if (!each.isSystem() && !each.isServiceAccount())
						watchService.watch(request, each, true);
				}
			}
	
			String senderName;
			String summary;
			if (user != null) {
				senderName = user.getDisplayName();
				summary = user.getDisplayName() + " " + event.getActivity();
			} else if (committer != null) {
				senderName = null;
				summary = committer.getDisplayName() + " " + event.getActivity();
			} else {
				senderName = null;
				summary = StringUtils.capitalize(event.getActivity());
			}
			
			var emojis = Emojis.getInstance();
			String replyAddress = mailService.getReplyAddress(request);
			boolean replyable = replyAddress != null;
	
			Set<User> reviewers = new HashSet<>();
			Set<User> assignees = new HashSet<>();
			if (event instanceof PullRequestOpened) {
				for (PullRequestReview review : request.getReviews()) {
					if (review.getStatus() == Status.PENDING)
						reviewers.add(review.getUser());
				}
				for (PullRequestAssignment assignment : request.getAssignments())
					assignees.add(assignment.getUser());
			} else if (event instanceof PullRequestChanged) {
				PullRequestChanged changeEvent = (PullRequestChanged) event;
				PullRequestChangeData changeData = changeEvent.getChange().getData();
				if ((changeData instanceof PullRequestApproveData
						|| changeData instanceof PullRequestRequestedForChangesData
						|| changeData instanceof PullRequestDiscardData)
						&& request.getSubmitter() != null && !notifiedUsers.contains(request.getSubmitter())) {
					String subject = String.format(
							"[Pull Request %s] (%s) %s", 
							request.getReference(),
							WordUtils.capitalize(changeData.getActivity()), 
							emojis.apply(request.getTitle()));
					String threadingReferences = String.format("<%s-%s@onedev>",
							changeData.getActivity().replace(' ', '-'), request.getUUID());
					EmailAddress emailAddress = request.getSubmitter().getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailService.sendMailAsync(Lists.newArrayList(emailAddress.getValue()),
								Lists.newArrayList(), Lists.newArrayList(), subject,
								getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null),
								getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null),
								replyAddress, senderName, threadingReferences);
					}
					notifiedUsers.add(request.getSubmitter());
				}
			} else if (event instanceof PullRequestAssigned) {
				assignees.add(((PullRequestAssigned) event).getAssignee());
			} else if (event instanceof PullRequestReviewRequested) {
				reviewers.add(((PullRequestReviewRequested) event).getReviewer());
			}
	
			for (User assignee : assignees) {
				if (!assignee.isServiceAccount())
					watchService.watch(request, assignee, true);
				if (!notifiedUsers.contains(assignee)) {
					String subject = String.format(
							"[Pull Request %s] (Assigned) %s",
							request.getReference(), 
							emojis.apply(request.getTitle()));
					String threadingReferences = String.format("<assigned-%s@onedev>", request.getUUID());
					String assignmentSummary;
					if (user != null)
						assignmentSummary = user.getDisplayName() + " assigned to you";
					else
						assignmentSummary = "Assigned to you";
					EmailAddress emailAddress = assignee.getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailService.sendMailAsync(Lists.newArrayList(emailAddress.getValue()),
								Lists.newArrayList(), Lists.newArrayList(), subject,
								getEmailBody(true, event, assignmentSummary, event.getHtmlBody(), url, replyable, null),
								getEmailBody(false, event, assignmentSummary, event.getTextBody(), url, replyable, null),
								replyAddress, senderName, threadingReferences);
					}
					notifiedUsers.add(assignee);
				}
			}
	
			for (User reviewer : reviewers) {
				if (!reviewer.isServiceAccount())
					watchService.watch(request, reviewer, true);
				if (!notifiedUsers.contains(reviewer)) {
					String subject = String.format(
							"[Pull Request %s] (Review Request) %s",
							request.getReference(), 
							emojis.apply(request.getTitle()));
					String threadingReferences = String.format("<review-invitation-%s@onedev>", request.getUUID());
					String reviewInvitationSummary;
					if (user != null)
						reviewInvitationSummary = user.getDisplayName() + " requested review from you";
					else
						reviewInvitationSummary = "Requested review from you";
	
					EmailAddress emailAddress = reviewer.getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailService.sendMailAsync(Lists.newArrayList(emailAddress.getValue()),
								Lists.newArrayList(), Lists.newArrayList(), subject,
								getEmailBody(true, event, reviewInvitationSummary, event.getHtmlBody(), url, replyable, null),
								getEmailBody(false, event, reviewInvitationSummary, event.getTextBody(), url, replyable, null),
								replyAddress, senderName, threadingReferences);
					}
					notifiedUsers.add(reviewer);
				}
			}
			
			if (event.getCommentText() instanceof MarkdownText) {
				MarkdownText markdown = (MarkdownText) event.getCommentText();
				for (String userName : new MentionParser().parseMentions(markdown.getRendered())) {
					User mentionedUser = userService.findByName(userName);
					if (mentionedUser != null) {
						mentionService.mention(request, mentionedUser);
						if (!mentionedUser.isServiceAccount())
							watchService.watch(request, mentionedUser, true);
						if (!isNotified(notifiedEmailAddresses, mentionedUser)) {
							String subject = String.format(
									"[Pull Request %s] (Mentioned You) %s", 
									request.getReference(), 
									emojis.apply(request.getTitle()));
							String threadingReferences = String.format("<mentioned-%s@onedev>", request.getUUID());
	
							EmailAddress emailAddress = mentionedUser.getPrimaryEmailAddress();
							if (emailAddress != null && emailAddress.isVerified()) {
								mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()),
										Sets.newHashSet(), Sets.newHashSet(), subject,
										getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null),
										getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null),
										replyAddress, senderName, threadingReferences);
							}
							notifiedUsers.add(mentionedUser);
						}
					}
				}
			}
	
			if (!event.isMinor()) {
				Collection<String> bccEmailAddresses = new HashSet<>();
				if (user != null && !notifiedUsers.contains(user) 
						&& user.getPrimaryEmailAddress() != null 
						&& user.getPrimaryEmailAddress().isVerified()) {
					bccEmailAddresses.add(user.getPrimaryEmailAddress().getValue());
				}
	
				for (PullRequestWatch watch : request.getWatches()) {
					Date visitDate = userInfoManager.getPullRequestVisitDate(watch.getUser(), request);
					Permission permission = new ProjectPermission(request.getProject(), new ReadCode());
					if (watch.isWatching()
							&& (visitDate == null || visitDate.before(event.getDate()))
							&& (!(event instanceof PullRequestUpdated) || !watch.getUser().equals(request.getSubmitter()))
							&& !notifiedUsers.contains(watch.getUser())
							&& !isNotified(notifiedEmailAddresses, watch.getUser())
							&& watch.getUser().asSubject().isPermitted(permission)) {
						EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified())
							bccEmailAddresses.add(emailAddress.getValue());
					}
				}
	
				if (!bccEmailAddresses.isEmpty()) {
					String subject = String.format(
							"[Pull Request %s] (%s) %s",
							request.getReference(), 
							(event instanceof PullRequestOpened) ? "Opened" : "Updated", 
							emojis.apply(request.getTitle()));
					String threadingReferences = "<" + request.getUUID() + "@onedev>";
					Unsubscribable unsubscribable = new Unsubscribable(mailService.getUnsubscribeAddress(request));
					String htmlBody = getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, unsubscribable);
					String textBody = getEmailBody(false, event, summary, event.getTextBody(), url, replyable, unsubscribable);
					mailService.sendMailAsync(
							Lists.newArrayList(), Lists.newArrayList(),
							bccEmailAddresses, subject, htmlBody, textBody,
							replyAddress, senderName, threadingReferences);
				}
			}			
		}
	}

} 
