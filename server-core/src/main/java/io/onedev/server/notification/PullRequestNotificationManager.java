package io.onedev.server.notification;

import static io.onedev.server.model.Build.Status.SUCCESSFUL;
import static io.onedev.server.model.PullRequestReview.Status.PENDING;
import static io.onedev.server.model.User.Type.AI;
import static io.onedev.server.model.support.AiSetting.PullRequestAssigneeResponsibility.FIX_FAILED_BUILDS;
import static io.onedev.server.model.support.AiSetting.PullRequestAssigneeResponsibility.MERGE_IF_ACCEPTABLE;
import static io.onedev.server.model.support.AiSetting.PullRequestAssigneeResponsibility.RESOLVE_MERGE_CONFLICTS;
import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isListening;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.shiro.authz.Permission;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.pullrequest.PullRequestAssigned;
import io.onedev.server.event.project.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.project.pullrequest.PullRequestMergePreviewUpdated;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.event.project.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.project.pullrequest.PullRequestUpdated;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.Build;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.AiSetting.PullRequestAssigneeResponsibility;
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
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestMentionService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.workspace.WorkspaceService;
import io.onedev.server.xodus.VisitInfoService;

@Singleton
public class PullRequestNotificationManager implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(PullRequestNotificationManager.class);
	
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

	@Inject
	private PullRequestCommentService pullRequestCommentService;

	@Inject
	private WorkspaceService workspaceService;

	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (event.getUser() == null || event.getUser().getType() != User.Type.SERVICE) {
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
	
			Collection<String> listeningEmailAddresses;
			if (event instanceof PullRequestCommentCreated)
				listeningEmailAddresses = ((PullRequestCommentCreated) event).getListeningEmailAddresses();
			else
				listeningEmailAddresses = new HashSet<>();
			
			Collection<User> notifiedUsers = Sets.newHashSet();
			if (user != null) {
				if (!user.isNotifyOwnEvents())
					notifiedUsers.add(user); 
				if (!user.isSystem())
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
					if (!each.isSystem())
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
					if (review.getStatus() == PENDING) {
						reviewers.add(review.getUser());
					}
				}
				for (PullRequestAssignment assignment : request.getAssignments())
					assignees.add(assignment.getUser());
			} else if (event instanceof PullRequestChanged) {
				PullRequestChanged changeEvent = (PullRequestChanged) event;
				PullRequestChangeData changeData = changeEvent.getChange().getData();
				if ((changeData instanceof PullRequestApproveData
							|| changeData instanceof PullRequestRequestedForChangesData
							|| changeData instanceof PullRequestDiscardData) 
						&& request.getSubmitter().getType() != AI 
						&& notifiedUsers.add(request.getSubmitter())) {
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
				}
				var submitter = request.getSubmitter();
				if (submitter.getType() == AI && changeData instanceof PullRequestRequestedForChangesData
						&& notifiedUsers.add(submitter)
						&& isAiEligible(null, request, submitter, true)
						&& canCreateWorkspace(submitter, request, true)) {
					workOnPullRequest(submitter, request, event);
				}
			} else if (event instanceof PullRequestAssigned) {
				assignees.add(((PullRequestAssigned) event).getAssignee());
			} else if (event instanceof PullRequestReviewRequested pullRequestReviewRequested) {
				reviewers.add(pullRequestReviewRequested.getReviewer());
			}
	
			for (User assignee : assignees) {
				watchService.watch(request, assignee, true);
				if (assignee.getType() != AI && notifiedUsers.add(assignee)) {
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
				} 
			}

			for (User reviewer : reviewers) {
				watchService.watch(request, reviewer, true);
				if (notifiedUsers.add(reviewer)) {
					if (reviewer.getType() != AI) {
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
					} else if (isAiEligible(null, request, reviewer, true) 
							&& canCreateWorkspace(reviewer, request, true)) {				
						workOnPullRequest(reviewer, request, event);
					}
				}
			}
				
			boolean codeCommentEvent = event instanceof PullRequestCodeCommentEvent;
			if (event.getCommentText() instanceof MarkdownText) {
				MarkdownText markdown = (MarkdownText) event.getCommentText();
				for (String userName : new MentionParser().parseMentions(markdown.getRendered())) {
					User mentionedUser = userService.findByName(userName);
					if (mentionedUser != null) {
						mentionService.mention(request, mentionedUser);
						watchService.watch(request, mentionedUser, true);
						if (!codeCommentEvent && notifiedUsers.add(mentionedUser)) {
							if (mentionedUser.getType() != AI) {
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
							} else if (user != null
									&& !user.isSystem()
									&& !user.equals(mentionedUser)
									&& isAiEligible(user, request, mentionedUser, true)
									&& canCreateWorkspace(mentionedUser, request, true)) {
								workOnPullRequest(mentionedUser, request, event);
							}
						}
					}
				}
			}
		
			if (!codeCommentEvent && !event.isMinor()) {
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
							&& !isListening(listeningEmailAddresses, watch.getUser())
							&& watch.getUser().asSubject().isPermitted(permission)) {
						if (watch.getUser().getType() != AI) {
							EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
							if (emailAddress != null && emailAddress.isVerified())
								bccEmailAddresses.add(emailAddress.getValue());
						} else if (event instanceof PullRequestCommentCreated
								&& user != null 
								&& !user.isSystem()
								&& !user.equals(watch.getUser()) 
								&& watch.getUser().getAiSetting().isProactive()
								&& isAiEligible(user, request, watch.getUser(), false)
								&& canCreateWorkspace(watch.getUser(), request, false)) {
							workOnPullRequest(watch.getUser(), request, event);
						}
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

			var maintenanceAi = getMaintenanceAi(request, event);
			if (maintenanceAi != null && notifiedUsers.add(maintenanceAi)
					&& isAiEligible(null, request, maintenanceAi, true)
					&& canCreateWorkspace(maintenanceAi, request, true)) {
				workOnPullRequest(maintenanceAi, request, event);
			}

			User mergeIfAcceptableAi = null;
			if (event instanceof PullRequestAssigned pullRequestAssigned) {
				var assignee = pullRequestAssigned.getAssignee();
				if (assignee.equals(request.getMergeAssigneeAi())) {
					mergeIfAcceptableAi = assignee;
				}
			} else if (event instanceof PullRequestOpened
					|| event instanceof PullRequestUpdated
					|| event instanceof PullRequestChanged pullRequestChanged && pullRequestChanged.getChange().getData() instanceof PullRequestApproveData
					|| event instanceof PullRequestBuildEvent pullRequestBuildEvent
							&& pullRequestBuildEvent.getBuild().isSuccessful()
							&& request.getBuildRequirement().getRequiredJobs().contains(pullRequestBuildEvent.getBuild().getJobName())
					|| event instanceof PullRequestMergePreviewUpdated && request.getMergePreview() != null && request.getMergePreview().getMergeCommitHash() != null) {
				mergeIfAcceptableAi = request.getMergeAssigneeAi();
			}

			if (mergeIfAcceptableAi != null
					&& request.checkMergeCondition() == null
					&& notifiedUsers.add(mergeIfAcceptableAi)
					&& isAiEligible(null, request, mergeIfAcceptableAi, true)
					&& canCreateWorkspace(mergeIfAcceptableAi, request, true)
					&& canWriteCode(mergeIfAcceptableAi, request)) {
				workOnPullRequest(mergeIfAcceptableAi, request, event);
			}
		}
	}

	/**
	 * Selects the AI responsible for an actionable build failure or merge conflict.
	 * An assignment can also trigger work on an existing problem if the new assignee
	 * is now responsible for it.
	 */
	@Nullable
	private User getMaintenanceAi(PullRequest request, PullRequestEvent event) {
		var preview = request.getMergePreview();
		var hasConflicts = preview != null && preview.getMergeCommitHash() == null;
		if (event instanceof PullRequestAssigned assigned) {
			var assignee = assigned.getAssignee();
			if (assignee.getType() == AI
					&& (request.getCurrentBuilds().stream().anyMatch(this::shouldFix)
							&& assignee.equals(request.getResponsibilityAi(it->it.getAiSetting().shouldFixFailedBuilds()))
						|| hasConflicts && assignee.equals(request.getResponsibilityAi(it->it.getAiSetting().shouldResolveMergeConflicts())))) {
				return assignee;
			}
		} else if (event instanceof PullRequestBuildEvent buildEvent && shouldFix(buildEvent.getBuild())) {
			return request.getResponsibilityAi(it->it.getAiSetting().shouldFixFailedBuilds());
		} else if (event instanceof PullRequestMergePreviewUpdated && hasConflicts) {
			return request.getResponsibilityAi(it->it.getAiSetting().shouldResolveMergeConflicts());
		}
		return null;
	}

	private boolean shouldFix(Build build) {
		return build.isFailed() && !build.getDependencies().stream().anyMatch(it -> it.isRequireSuccessful() && it.getDependency().isFinished() && it.getDependency().getStatus() != SUCCESSFUL);		
	}

	@Nullable
	private String getWorkspaceBranch(PullRequest request) {
		if (request.getSourceProject() != null) {
			return request.getSourceBranch();
		} else {
			return null;
		}
	}

	private boolean canWriteCode(User ai, PullRequest request) {
		if (SecurityUtils.canWriteCode(ai.asSubject(), request.getTargetProject())) {
			return true;
		} else {
			createComment(ai, request, "I need write code permission in target project to do the job");				
			return false;
		}
	}

	private boolean isAiEligible(@Nullable User user, PullRequest request, User ai, boolean commentOnError) {
		if (ai.isDisabled()) {
			if (commentOnError) 
				createComment(ai, request, "I'm disabled, and cannot do the job");				
			return false;
		} else if (user != null && user.getId() > 0) {
			if (user.isEntitledToAi(ai)) {
				return true;
			} else {
				if (commentOnError)
					createComment(ai, request, "@%s is not entitled to interact with me".formatted(user.getName()));				
				return false;
			}
		} else {
			if (request.getProject().isEntitledToAi(ai)) {
				return true;
			} else {
				if (commentOnError)
					createComment(ai, request, "I'm not entitled to work on this project");				
				return false;
			}
		}
	}

	private void workOnPullRequest(User ai, PullRequest request, PullRequestEvent event) {
		var participatingUserIds = event.getParticipatingUserIds();
		var maxLoopCount = ai.getAiSetting().getMaxLoopCount();
		var awakenCount = participatingUserIds.stream().filter(ai.getId()::equals).count();
		if (awakenCount >= maxLoopCount) {
			createComment(ai, request, "I reached the maximum awaken count of %d in the current event chain"
					.formatted(maxLoopCount));
			return;
		}

		try {
			var commitId = request.getLatestUpdate().getHeadCommit().copy();
			workspaceService.workOnPullRequest(ai, request, commitId, getWorkspaceBranch(request),
					event.getDate(), participatingUserIds);
		} catch (Throwable t) {
			var explicitException = ExceptionUtils.find(t, ExplicitException.class);
			if (explicitException != null) {
				createComment(ai, request, explicitException.getMessage());
			} else {
				logger.error("Error doing job via AI user", t);
				createComment(ai, request, "Failed to do the job, check server log for details");
			}
		}
	}

	private void createComment(User ai, PullRequest request, String comment) {
		pullRequestCommentService.create(userService.getSystem(), request,
				"On behalf of AI user \"%s\": %s".formatted(ai.getDisplayName(), comment));
	}

	private boolean canCreateWorkspace(User ai, PullRequest request, boolean commentOnError) {
		if (!SecurityUtils.canCreateWorkspaces(ai.asSubject(), request.getProject())) {			
			if (commentOnError)
				createComment(ai, request, "I need create workspace permission in this project to do the job");				
			return false;
		}
		if (request.getProject().getDefaultBranch() == null) {
			if (commentOnError)
				createComment(ai, request, "I need to create workspace to do the job, but the project doesn't have code yet");				
			return false;
		}
		return true;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestNotificationManager.class);
	}

} 
