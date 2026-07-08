package io.onedev.server.notification;

import static io.onedev.server.model.PullRequestReview.Status.PENDING;
import static io.onedev.server.model.PullRequestReview.Status.REQUESTED_FOR_CHANGES;
import static io.onedev.server.model.User.Type.AI;
import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isListening;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.shiro.authz.Permission;
import org.eclipse.jgit.lib.ObjectId;
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
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
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
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.workspace.TaskFailedCallback;
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
	private PullRequestService pullRequestService;

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
				if (!user.isSystem() && user.getType() != AI)
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
					if (!each.isSystem() && each.getType() != AI)
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
				if ((changeData instanceof PullRequestRequestedForChangesData || changeData instanceof PullRequestApproveData) 
						&& user.getType() == AI
						&& request.getSubmitter().getType() == AI 
						&& request.getReviews().stream().noneMatch(it -> it.getUser().getType() == AI && it.getStatus() == PENDING)
						&& request.getReviews().stream().anyMatch(it -> it.getUser().getType() == AI && it.getStatus() == REQUESTED_FOR_CHANGES)
						&& notifiedUsers.add(request.getSubmitter())
						&& isAiEntitled(null, request, request.getSubmitter())
						&& canCreateWorkspace(request.getSubmitter(), request)) {
					var prompt = """
							Work on pull request %d to address user feedbacks since your last work. \
							If code is changed as a result of this, make sure to not mention users \
							in your comments; otherwise, mention users only when you expect a response. \
							Make sure to submit work afterwards without confirmation."""
							.formatted(request.getNumber());
					var commitId = request.getLatestUpdate().getHeadCommit().copy();
					runPrompt(request.getSubmitter(), request, commitId, getWorkspaceBranch(request), prompt);
				}
				if ((changeData instanceof PullRequestRequestedForChangesData) 
						&& user.getType() != AI
						&& request.getSubmitter().getType() == AI 
						&& notifiedUsers.add(request.getSubmitter())
						&& isAiEntitled(null, request, request.getSubmitter())
						&& canCreateWorkspace(request.getSubmitter(), request)) {
					var prompt = """
							Work on pull request %d to address %s's latest concern. \
							Make sure to submit work afterwards without confirmation."""
							.formatted(request.getNumber(), user.getName());
					var commitId = request.getLatestUpdate().getHeadCommit().copy();
					runPrompt(request.getSubmitter(), request, commitId, getWorkspaceBranch(request), prompt);
				}
		} else if (event instanceof PullRequestAssigned) {
				assignees.add(((PullRequestAssigned) event).getAssignee());
			} else if (event instanceof PullRequestReviewRequested pullRequestReviewRequested) {
				reviewers.add(pullRequestReviewRequested.getReviewer());
			}
	
			for (User assignee : assignees) {
				if (assignee.getType() != AI) {
					watchService.watch(request, assignee, true);
					if (notifiedUsers.add(assignee)) {
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
			}
	
			for (User reviewer : reviewers) {
				if (reviewer.getType() != AI) 
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
					} else if (isAiEntitled(null, request, reviewer) 
							&& canCreateWorkspace(reviewer, request)) {				
						var prompt = """
								Work on pull request %d to perform the review. \
								Stay on current checkout and do not modify code. \
								If review state is changed as a result of this, \
								make sure to not mention users in the review notes or comments; \
								otherwise, mention users only when you expect a response. \
								Make sure to submit work afterwards without confirmation."""
								.formatted(request.getNumber(), request.getSubmitter().getName());
						var commitId = request.getLatestUpdate().getHeadCommit().copy();						
						runPrompt(reviewer, request, commitId, getWorkspaceBranch(request), prompt);
					}
				}
			}
			
			if (event.getCommentText() instanceof MarkdownText) {
				MarkdownText markdown = (MarkdownText) event.getCommentText();
				for (String userName : new MentionParser().parseMentions(markdown.getRendered())) {
					User mentionedUser = userService.findByName(userName);
					if (mentionedUser != null) {
						mentionService.mention(request, mentionedUser);
						if (mentionedUser.getType() != AI) 
							watchService.watch(request, mentionedUser, true);
						if (notifiedUsers.add(mentionedUser)) {
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
							} else if (!(event instanceof PullRequestCodeCommentEvent) 
									&& isAiEntitled(user, request, mentionedUser)
									&& canCreateWorkspace(mentionedUser, request)) {
								addressConcern(mentionedUser, user, request);
							}
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
							&& !isListening(listeningEmailAddresses, watch.getUser())
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

			if (request.getSubmitter().getType() == AI) {
				if (event instanceof PullRequestBuildEvent pullRequestBuildEvent 
							&& pullRequestBuildEvent.getBuild().isFailed() 
							&& !pullRequestBuildEvent.getBuild().getDependencies().stream().anyMatch(it -> 
									it.isRequireSuccessful() && it.getDependency().isFinished() && it.getDependency().getStatus() != Build.Status.SUCCESSFUL)
							&& notifiedUsers.add(request.getSubmitter())
							&& isAiEntitled(null, request, request.getSubmitter())
							&& canCreateWorkspace(request.getSubmitter(), request)) {
					var prompt = """
							Work on pull request %d to fix failure of build %d. \
							Make sure to submit work afterwards without confirmation."""
							.formatted(request.getNumber(), pullRequestBuildEvent.getBuild().getNumber());
					var commitId = request.getLatestUpdate().getHeadCommit().copy();
					runPrompt(request.getSubmitter(), request, commitId, getWorkspaceBranch(request), prompt);
				}
				if (event instanceof PullRequestMergePreviewUpdated && request.getMergePreview() != null 
							&& request.getMergePreview().getMergeCommitHash() == null 
							&& notifiedUsers.add(request.getSubmitter())
							&& isAiEntitled(null, request, request.getSubmitter())
							&& canCreateWorkspace(request.getSubmitter(), request)) {
					var prompt = """
						Work on pull request %d to resolve merge conflict. \
						Make sure to submit work afterwards without confirmation."""
						.formatted(request.getNumber());
					var commitId = request.getLatestUpdate().getHeadCommit().copy();
					runPrompt(request.getSubmitter(), request, commitId, getWorkspaceBranch(request), prompt);					
				}
			}
			
			User aiAssignee = null;
			if (event instanceof PullRequestAssigned pullRequestAssigned) {
				if (pullRequestAssigned.getAssignee().getType() == AI) 
					aiAssignee = pullRequestAssigned.getAssignee();
			} else if (event instanceof PullRequestOpened 
					|| event instanceof PullRequestChanged pullRequestChanged && pullRequestChanged.getChange().getData() instanceof PullRequestApproveData
					|| event instanceof PullRequestBuildEvent pullRequestBuildEvent && pullRequestBuildEvent.getBuild().isSuccessful()) {
				for (PullRequestAssignment assignment : request.getAssignments()) {
					if (assignment.getUser().getType() == AI) {
						aiAssignee = assignment.getUser();
						break;
					}
				}
			}
			if (aiAssignee != null && request.checkMergeCondition() == null 
					&& notifiedUsers.add(aiAssignee)
					&& isAiEntitled(null, request, aiAssignee) 
					&& canWriteCode(aiAssignee, request, request.getTargetProject())) {
				var prompt = """
						Work on pull request %d to review and merge if ready. \
						Otherwise, mention @%s in a PR comment to request changes. \
						Stay on current checkout and do not modify code. \
						Make sure to submit work afterwards without confirmation."""
						.formatted(request.getNumber(), request.getSubmitter().getName());
				var commitId = request.getLatestUpdate().getHeadCommit().copy();
				runPrompt(aiAssignee, request, commitId, getWorkspaceBranch(request), prompt);								
			}
		}
	}

	@Nullable
	private String getWorkspaceBranch(PullRequest request) {
		if (request.getSourceProject() != null) {
			return request.getSourceBranch();
		} else {
			return null;
		}
	}

	private boolean canWriteCode(User ai, PullRequest request, Project project) {
		if (SecurityUtils.canWriteCode(ai.asSubject(), project)) {
			return true;
		} else {
			createComment(ai, request, "I need write code permission in target project to do the job");				
			return false;
		}
	}

	private boolean isAiEntitled(@Nullable User user, PullRequest request, User ai) {
		if (user != null && user.getId() > 0) {
			if (user.isEntitledToAi(ai)) {
				return true;
			} else {
				createComment(ai, request, "@%s you are not entitled to interact with me".formatted(user.getName()));				
				return false;
			}
		} else {
			if (request.getProject().isEntitledToAi(ai)) {
				return true;
			} else {
				createComment(ai, request, "I'm not entitled to work on this project");				
				return false;
			}
		}
	}

	private void addressConcern(User ai, User commenter, PullRequest request) {		
		List<String> prompts = new ArrayList<>();
		if (request.getSubmitter().equals(ai)) {
			prompts.add("Work on pull request %d as submitter to address %s's latest concern."
					.formatted(request.getNumber(), commenter.getName()));
		} else {
			prompts.add("Work on pull request %d to address %s's latest concern."
					.formatted(request.getNumber(), commenter.getName()));
		}
		prompts.add("Do not switch checkout if the concern does not require you to write code.");
		if (request.getAssignees().contains(ai) && request.checkMergeCondition() == null 
				&& canWriteCode(ai, request, request.getTargetProject())) {
			prompts.add("Also merge the pull request if your review passes.");
		}

		prompts.add("""
			Mention the user only when you expect a response. \
			Make sure to submit work afterwards without confirmation.""");
		
		var concatenatedPrompts = String.join(" ", prompts);
		var commitId = request.getLatestUpdate().getHeadCommit().copy();
		runPrompt(ai, request, commitId, getWorkspaceBranch(request), concatenatedPrompts);					
	}

	private void runPrompt(User ai, PullRequest request, ObjectId commitId, @Nullable String branch, String prompt) {
		try {
			var taskFailedCallback = newTaskFailedCallback(ai.getId(), request.getId());
			workspaceService.runPrompt(ai, request.getProject(), null, request, commitId, branch, prompt, taskFailedCallback);
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
		if (SecurityUtils.canReadCode(ai.asSubject(), request.getProject())) {
			pullRequestCommentService.create(ai, request, comment);
		} else {
			pullRequestCommentService.create(userService.getSystem(), request, "_Commenting on behalf of **%s** as the user does not even have permission to post here._\n\n%s".formatted(ai.getName(), comment));
		}
	}

	private boolean canCreateWorkspace(User ai, PullRequest request) {
		if (!SecurityUtils.canCreateWorkspaces(ai.asSubject(), request.getProject())) {			
			createComment(ai, request, "I need create workspace permission in this project to do the job");				
			return false;
		}
		if (request.getProject().getDefaultBranch() == null) {
			createComment(ai, request, "I need to create workspace to do the job, but the project doesn't have code yet");				
			return false;
		}
		return true;
	}

	private TaskFailedCallback newTaskFailedCallback(Long aiId, Long requestId) {
		return new TaskFailedCallback() {
		
			public void onTaskFailed(String workspaceReference) {							
				createComment(userService.load(aiId), pullRequestService.load(requestId), "Failed to do the job, please open workspace %s for details".formatted(workspaceReference));
			}

		};
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestNotificationManager.class);
	}

} 
