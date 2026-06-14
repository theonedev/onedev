package io.onedev.server.notification;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isNotified;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.shiro.authz.Permission;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.ai.AiTask;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolUtils;
import io.onedev.server.ai.responsehandlers.AddCodeCommentReply;
import io.onedev.server.ai.responsehandlers.AddPullRequestComment;
import io.onedev.server.ai.taskchecker.NoopTaskChecker;
import io.onedev.server.ai.taskchecker.PullRequestReviewTaskChecker;
import io.onedev.server.ai.tools.codecomment.GetCodeComment;
import io.onedev.server.ai.tools.codecomment.GetCodeCommentReplies;
import io.onedev.server.ai.tools.codecomment.ResolveCodeComment;
import io.onedev.server.ai.tools.codecomment.UnresolveCodeComment;
import io.onedev.server.ai.tools.pullrequest.ApprovePullRequest;
import io.onedev.server.ai.tools.pullrequest.GetPullRequest;
import io.onedev.server.ai.tools.pullrequest.GetPullRequestComments;
import io.onedev.server.ai.tools.pullrequest.RequestChangesForPullRequestTool;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.pullrequest.PullRequestAssigned;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentReplyCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.event.project.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.project.pullrequest.PullRequestUpdated;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
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
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.service.PullRequestMentionService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.xodus.VisitInfoService;

@Singleton
public class PullRequestNotificationManager implements Serializable {

	private static final String AI_REVIEW_DECISION_TOOLS = """
			one of these tools, and call it only once: `{0}` if you are satisfied with the change, or `{1}` if you \
			think it needs more work. If you are unsure, explain what is unclear via final response""";

	private static final String AI_PROMPT_MENTIONED_IN_PULL_REQUEST = """
			You are mentioned in a pull request. The content mentioning you is presented as user \
			prompt. Use existing comments as conversation context. Use getPullRequest for PR details; \
			use getDiffPatch, getFileContent, querySymbolDefinitions, or queryCodeSnippets as needed to \
			inspect the change. When feedback should be tied to specific lines, use \
			getPullRequestCodeComments and addPullRequestCodeComment \
			(range on the right side of the diff, 1-based at PR head); use \
			addCodeCommentReply, resolveCodeComment, or \
			unresolveCodeComment to continue or triage existing code-comment threads.""";

	private static final String AI_PROMPT_MENTIONED_IN_CODE_COMMENT = """
			You are mentioned in a pull request code comment. The content mentioning you is presented \
			as user prompt. Use existing comment and replies as conversation context. Call relevant \
			tools to inspect associated pull request if necessary. Use resolveCodeComment or \
			unresolveCodeComment when the discussion outcome is to close or reopen the thread""";

	private static final String AI_PROMPT_REVIEW_PULL_REQUEST = """
			Review current pull request. Use getPullRequest for PR details; use getDiffPatch, getFileContent, \
			querySymbolDefinitions, and queryCodeSnippets as needed to inspect the change. \
			Use getPullRequestComments and getPullRequestCodeComments for \
			discussion context. Prefer line-anchored feedback: call addPullRequestCodeComment for \
			each distinct issue (the line range must lie on the right side of the PR diff—added or \
			unchanged context—using 1-based line numbers in the file at the PR head). To follow up on \
			an existing anchor, use addCodeCommentReply with the comment id; use \
			resolveCodeComment or unresolveCodeComment when a prior thread should be closed or reopened. \
			""";

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
	
			Collection<String> notifiedEmailAddresses;
			if (event instanceof PullRequestCommentCreated)
				notifiedEmailAddresses = ((PullRequestCommentCreated) event).getNotifiedEmailAddresses();
			else
				notifiedEmailAddresses = new HashSet<>();
			
			Collection<User> notifiedUsers = Sets.newHashSet();
			if (user != null) {
				if (!user.isNotifyOwnEvents() || isNotified(notifiedEmailAddresses, user))
					notifiedUsers.add(user); 
				if (!user.isSystem() && user.getType() != User.Type.AI)
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
					if (!each.isSystem() && each.getType() != User.Type.AI)
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
					if (request.getSubmitter().getType() != User.Type.AI) {
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
					} else if (changeData instanceof PullRequestRequestedForChangesData 
							&& isAiEntitled(request, request.getSubmitter())) {
						new AddPullRequestComment(request.getId()).onResponse(
							request.getSubmitter(), 
							"I don't know how to improve the pull request yet");
					}
					notifiedUsers.add(request.getSubmitter());
				}
			} else if (event instanceof PullRequestAssigned) {
				assignees.add(((PullRequestAssigned) event).getAssignee());
			} else if (event instanceof PullRequestReviewRequested) {
				reviewers.add(((PullRequestReviewRequested) event).getReviewer());
			}
	
			for (User assignee : assignees) {
				if (assignee.getType() != User.Type.AI)
					watchService.watch(request, assignee, true);
				if (!notifiedUsers.contains(assignee)) {
					if (assignee.getType() != User.Type.AI) {
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
					} else if (isAiEntitled(request, assignee)) {
						new AddPullRequestComment(request.getId()).onResponse(assignee, "I don't know how to work as assignee yet");
					}
					notifiedUsers.add(assignee);
				}
			}
	
			for (User reviewer : reviewers) {
				if (reviewer.getType() != User.Type.AI)
					watchService.watch(request, reviewer, true);
				if (!notifiedUsers.contains(reviewer)) {
					if (reviewer.getType() != User.Type.AI) {
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
					} else if (isAiEntitled(request, reviewer)) {
						var tools = new ArrayList<TaskTool>();
						addPullRequestInspectionTools(tools, request, true, true);
						var task = new AiTask(
							null,
							aiPromptReviewPullRequest(),
							tools,
							new PullRequestReviewTaskChecker(),
							new AddPullRequestComment(request.getId()));
						userService.execute(reviewer, task);
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
						if (mentionedUser.getType() != User.Type.AI) 
							watchService.watch(request, mentionedUser, true);
						if (!isNotified(notifiedEmailAddresses, mentionedUser)) {
							if (mentionedUser.getType() != User.Type.AI) {
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
							} else if (isAiEntitled(request, mentionedUser)) {
								if (event instanceof PullRequestOpened) {
									var tools = new ArrayList<TaskTool>();
									addPullRequestInspectionTools(tools, request, false, true);
									var task = new AiTask(
										AI_PROMPT_MENTIONED_IN_PULL_REQUEST,
										event.getTextBody(),
										tools,
										new NoopTaskChecker(),
										new AddPullRequestComment(request.getId()));
									userService.execute(mentionedUser, task);
								} else if (event instanceof PullRequestCommentCreated) {
									var review = request.getReview(mentionedUser);
									var pendingReview = review != null && review.getStatus() == PullRequestReview.Status.PENDING;
									var tools = new ArrayList<TaskTool>();
									addPullRequestInspectionTools(tools, request, pendingReview, true);
									var task = new AiTask(
										aiPromptMentionedInPullRequest(pendingReview),
										event.getTextBody(),
										tools,
										new PullRequestReviewTaskChecker(),
										new AddPullRequestComment(request.getId()));
									userService.execute(mentionedUser, task);
								} else if (event instanceof PullRequestCodeCommentCreated || event instanceof PullRequestCodeCommentReplyCreated) {
									var tools = new ArrayList<TaskTool>();
									var codeCommentEvent = (PullRequestCodeCommentEvent) event;
									var comment = codeCommentEvent.getComment();
									tools.add(new GetCodeComment(comment.getId()));
									tools.add(new GetCodeCommentReplies(comment.getId()));
									tools.add(new ResolveCodeComment());
									tools.add(new UnresolveCodeComment());
									addPullRequestInspectionTools(tools, request, false, false);
									var task = new AiTask(
										aiPromptMentionedInCodeComment(false),
										event.getTextBody(),
										tools,
										new NoopTaskChecker(),
										new AddCodeCommentReply(comment.getId()));
									userService.execute(mentionedUser, task);
								}
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

	private static String formatAiReviewDecisionTools() {
		return MessageFormat.format(AI_REVIEW_DECISION_TOOLS,
				ApprovePullRequest.TOOL_NAME, RequestChangesForPullRequestTool.TOOL_NAME);
	}

	private static String formatAiReviewDecisionWhenRequested() {
		return "If you are requested to review the pull request, record your final decision by calling "
				+ formatAiReviewDecisionTools();
	}

	private static String aiPromptMentionedInPullRequest(boolean pendingReview) {
		if (pendingReview)
			return AI_PROMPT_MENTIONED_IN_PULL_REQUEST + "\n\n" + formatAiReviewDecisionWhenRequested();
		return AI_PROMPT_MENTIONED_IN_PULL_REQUEST;
	}

	private static String aiPromptMentionedInCodeComment(boolean pendingReview) {
		if (pendingReview)
			return AI_PROMPT_MENTIONED_IN_CODE_COMMENT + ".\n\n" + formatAiReviewDecisionWhenRequested();
		return AI_PROMPT_MENTIONED_IN_CODE_COMMENT;
	}

	private static String aiPromptReviewPullRequest() {
		return AI_PROMPT_REVIEW_PULL_REQUEST + "Record your final decision by calling " + formatAiReviewDecisionTools();
	}

	private static void addPullRequestInspectionTools(ArrayList<TaskTool> tools, PullRequest request,
			boolean includeReviewTools, boolean includeCodeCommentTools) {
		long requestId = request.getId();
		var projectId = request.getProject().getId();
		var oldCommitId = ObjectId.fromString(request.getBaseCommitHash());
		var newCommitId = ObjectId.fromString(request.getLatestUpdate().getHeadCommitHash());
		tools.add(new GetPullRequest(requestId));
		tools.add(new GetPullRequestComments(requestId));
		if (includeReviewTools)
			tools.addAll(ToolUtils.getPullRequestReviewTools(requestId));
		if (includeCodeCommentTools)
			tools.addAll(ToolUtils.getPullRequestCodeCommentTools(requestId));
		tools.addAll(ToolUtils.getDiffTools(projectId, oldCommitId, newCommitId, requestId));
	}

	private boolean isAiEntitled(PullRequest request, User ai) {
		if (request.getProject().isEntitledToAi(ai)) {
			return true;
		} else {
			new AddPullRequestComment(request.getId()).onResponse(ai, "Sorry but this project is not entitled to access me");				
			return false;
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestNotificationManager.class);
	}

} 
