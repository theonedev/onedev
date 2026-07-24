package io.onedev.server.notification;

import static io.onedev.server.model.User.Type.AI;
import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isListening;
import static io.onedev.server.util.EmailAddressUtils.describe;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

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
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.project.issue.IssuesMoved;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.IssueAuthorizationService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueMentionService;
import io.onedev.server.service.IssueQueryPersonalizationService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.workspace.TaskFailedCallback;
import io.onedev.server.workspace.WorkspaceService;
import io.onedev.server.xodus.VisitInfoService;

@Singleton
public class IssueNotificationManager implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(IssueNotificationManager.class);
	
	@Inject
	private MailService mailService;

	@Inject
	private IssueAuthorizationService authorizationService;

	@Inject
	private IssueWatchService watchService;

	@Inject
	private UserService userService;

	@Inject
	private GroupService groupService;

	@Inject
	private VisitInfoService userInfoManager;

	@Inject
	private IssueMentionService mentionService;

	@Inject
	private IssueQueryPersonalizationService queryPersonalizationService;

	@Inject
	private IssueService issueService;

	@Inject
	private IssueCommentService issueCommentService;

	@Inject
	private WorkspaceService workspaceService;

	@Inject
	private UrlService urlService;

	@Inject
	private SettingService settingService;

	private void setupWatches(Issue issue) {
		for (Map.Entry<User, Boolean> entry : new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return queryPersonalizationService.query(new ProjectScope(issue.getProject(), true, true));
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
				return IssueQuery.parse(issue.getProject(), queryString, option, true);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return issue.getProject().getNamedIssueQueries();
			}

		}.getWatches().entrySet()) {
			if (SecurityUtils.canAccessIssue(entry.getKey().asSubject(), issue))
				watchService.watch(issue, entry.getKey(), entry.getValue());
		}

		for (Map.Entry<User, Boolean> entry : new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return userService.query().stream().map(it -> it.getIssueQueryPersonalization()).collect(Collectors.toList());
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				IssueQueryParseOption option = new IssueQueryParseOption().withCurrentBuildCriteria(true);
				return IssueQuery.parse(null, queryString, option, true);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return settingService.getIssueSetting().getNamedQueries();
			}

		}.getWatches().entrySet()) {
			if (SecurityUtils.canAccessIssue(entry.getKey().asSubject(), issue))
				watchService.watch(issue, entry.getKey(), entry.getValue());
		}
	}
	
	private String getSenderName(InternetAddress internetAddress) {
		var senderName = internetAddress.getPersonal();
		if (senderName == null)
			senderName = StringUtils.substringBefore(internetAddress.getAddress(), "@");
		return senderName;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		if (event.isMinor() || !event.isSendNotifications())
			return;

		Issue issue = event.getIssue();
		User user = event.getUser();

		String url = event.getUrl();

		String senderName;
		String summary;
		IssueComment comment;
		if (event instanceof IssueOpened && issue.getOnBehalfOf() != null) {
			senderName = getSenderName(issue.getOnBehalfOf());
			summary = describe(issue.getOnBehalfOf(), false) + " " + event.getActivity();
		} else if (event instanceof IssueCommentCreated && (comment = ((IssueCommentCreated) event).getComment()).getOnBehalfOf() != null) {
			senderName = getSenderName(comment.getOnBehalfOf());
			summary = describe(comment.getOnBehalfOf(), false) + " " + event.getActivity();
		} else if (user != null) {
			senderName = user.getDisplayName();
			summary = senderName + " " + event.getActivity();
		} else {
			senderName = null;
			summary = StringUtils.capitalize(event.getActivity());
		}

		setupWatches(issue);

		Collection<String> listeningEmailAddresses;
		if (event instanceof IssueCommentCreated)
			listeningEmailAddresses = ((IssueCommentCreated) event).getListeningEmailAddresses();
		else if (event instanceof IssueOpened)
			listeningEmailAddresses = ((IssueOpened) event).getListeningEmailAddresses();
		else
			listeningEmailAddresses = new HashSet<>();
		
		Collection<User> notifiedUsers = Sets.newHashSet();
		if (user != null) {
			if (!user.isNotifyOwnEvents())
				notifiedUsers.add(user); 
			if (!user.isSystem())
				watchService.watch(issue, user, true);
		}

		var emojis = Emojis.getInstance();
		Map<String, Group> newGroups = event.getNewGroups();
		Map<String, Collection<User>> newUsers = event.getNewUsers();
		
		String replyAddress = mailService.getReplyAddress(issue);
		boolean replyable = replyAddress != null;
		for (Map.Entry<String, Group> entry: newGroups.entrySet()) {
			String subject = String.format(
					"[Issue %s] (%s: You) %s", 
					issue.getReference(), 
					entry.getKey(), 
					emojis.apply(issue.getTitle()));
			String threadingReferences = String.format("<you-in-field-%s-%s@onedev>", entry.getKey(), issue.getUUID());
			for (User member: entry.getValue().getMembers()) {
				if (notifiedUsers.add(member)) {
					if (member.getType() != AI) {
						EmailAddress emailAddress = member.getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified()) {
							mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
									Lists.newArrayList(), Lists.newArrayList(), subject, 
									getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
									getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
									replyAddress, senderName, threadingReferences);
						}
					} else if (isAiEligible(null, issue, member, true) && canCreateWorkspace(member, issue, true)) { 
						onFieldSet(member, issue, entry.getKey(), event.getParticipatingUserIds());
					}
				}
			}
			
			for (User member: entry.getValue().getMembers()) {
				watchService.watch(issue, member, true);
				authorizationService.authorize(issue, member);
			}
		}
		
		for (Map.Entry<String, Collection<User>> entry: newUsers.entrySet()) {
			String subject = String.format(
					"[Issue %s] (%s: You) %s", 
					issue.getReference(), 
					entry.getKey(), 
					emojis.apply(issue.getTitle()));
			String threadingReferences = String.format("<you-in-field-%s-%s@onedev>", entry.getKey(), issue.getUUID());
			for (User assignedUser: entry.getValue()) {
				if (notifiedUsers.add(assignedUser)) {
					if (assignedUser.getType() != AI) {
						EmailAddress emailAddress = assignedUser.getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified()) {
							mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
									Lists.newArrayList(), Lists.newArrayList(), subject, 
									getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
									getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
									replyAddress, senderName, threadingReferences);
						}
					} else if (isAiEligible(null, issue, assignedUser, true) && canCreateWorkspace(assignedUser, issue, true)) { 
						onFieldSet(assignedUser, issue, entry.getKey(), event.getParticipatingUserIds());
					}
				} 
			}

			for (User each: entry.getValue()) {
				watchService.watch(issue, each, true);
				authorizationService.authorize(issue, each);
			}
		}
		
		if (event.getCommentText() instanceof MarkdownText) {
			MarkdownText markdown = (MarkdownText) event.getCommentText();
			for (String userName: new MentionParser().parseMentions(markdown.getRendered())) {
				User mentionedUser = userService.findByName(userName);
				if (mentionedUser != null) {
					mentionService.mention(issue, mentionedUser);
					authorizationService.authorize(issue, mentionedUser);
					watchService.watch(issue, mentionedUser, true);
					if (notifiedUsers.add(mentionedUser)) {						
						if (mentionedUser.getType() != AI) {
							String subject = String.format(
									"[Issue %s] (Mentioned You) %s", 
									issue.getReference(), 
									emojis.apply(issue.getTitle()));
							String threadingReferences = String.format("<mentioned-%s@onedev>", issue.getUUID());
							
							EmailAddress emailAddress = mentionedUser.getPrimaryEmailAddress();
							if (emailAddress != null && emailAddress.isVerified()) {
								mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
										Sets.newHashSet(), Sets.newHashSet(), subject, 
										getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
										getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null),
										replyAddress, senderName, threadingReferences);
							}
						} else if (user != null
								&& (!user.isSystem()
									|| event instanceof IssueOpened issueOpened && issueOpened.getIssue().getOnBehalfOf() != null
									|| event instanceof IssueCommentCreated issueCommentCreated && issueCommentCreated.getComment().getOnBehalfOf() != null)
								&& !user.equals(mentionedUser) 
								&& isAiEligible(user, issue, mentionedUser, true)
								&& canCreateWorkspace(mentionedUser, issue, true)) {
							onAiMentioned(mentionedUser, user, issue, event.getParticipatingUserIds());
						}
					}
				}
			}
		}

		Collection<String> bccEmailAddresses = new HashSet<>();
		
		if (user != null && !notifiedUsers.contains(user)
				&& user.getPrimaryEmailAddress() != null 
				&& user.getPrimaryEmailAddress().isVerified()) {
			bccEmailAddresses.add(user.getPrimaryEmailAddress().getValue());
		}
		
		for (IssueWatch watch: issue.getWatches()) {
			Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
			if (watch.isWatching()
						&& (visitDate == null || visitDate.before(event.getDate()))
						&& !notifiedUsers.contains(watch.getUser())
						&& !isListening(listeningEmailAddresses, watch.getUser())
						&& SecurityUtils.canAccessIssue(watch.getUser().asSubject(), issue)) {
				if (watch.getUser().getType() != AI) {
					EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified())
						bccEmailAddresses.add(emailAddress.getValue());
				} else if (user != null
						&& (!user.isSystem()
							|| event instanceof IssueOpened issueOpened && issueOpened.getIssue().getOnBehalfOf() != null
							|| event instanceof IssueCommentCreated issueCommentCreated && issueCommentCreated.getComment().getOnBehalfOf() != null)
						&& !user.equals(watch.getUser()) 
						&& watch.getUser().getAiSetting().isProactive()
						&& isAiEligible(user, issue, watch.getUser(), false) 
						&& canCreateWorkspace(watch.getUser(), issue, false)) {
					onAiNotified(watch.getUser(), user, issue, event.getParticipatingUserIds());
				}
			}
		}
		
		var notifiedEmailAddresses = new HashSet<String>();
		for (var notifiedUser: notifiedUsers) {
			for (var emailAddress: notifiedUser.getEmailAddresses()) {
				if (emailAddress.isVerified())
					notifiedEmailAddresses.add(emailAddress.getValue());
			}
		}
		for (var participant: issue.getExternalParticipants()) {
			if (!listeningEmailAddresses.contains(participant.getAddress()) 
					&& !notifiedEmailAddresses.contains(participant.getAddress())) {
				bccEmailAddresses.add(participant.getAddress());
			}
		}

		if (!bccEmailAddresses.isEmpty()) {
			String subject = String.format(
					"[Issue %s] (%s) %s", 
					issue.getReference(), 
					(event instanceof IssueOpened)?"Opened":"Updated", 
					emojis.apply(issue.getTitle())); 

			Unsubscribable unsubscribable = new Unsubscribable(mailService.getUnsubscribeAddress(issue));
			String htmlBody = getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, unsubscribable);
			String textBody = getEmailBody(false, event, summary, event.getTextBody(), url, replyable, unsubscribable);

			String threadingReferences = issue.getThreadingReferences();
			mailService.sendMailAsync(Sets.newHashSet(), Sets.newHashSet(), 
					bccEmailAddresses, subject, htmlBody, textBody, 
					replyAddress, senderName, threadingReferences);
		}
	}

	private Pair<ObjectId, String> getWorkspaceCommitIdAndBranch(Issue issue) {
		String branch = issue.getBranch();									
		ObjectId commitId;
		if (branch == null) {
			commitId = issue.getFieldCommitId();
			if (commitId == null) 
				commitId = issue.getProject().getObjectId(Preconditions.checkNotNull(issue.getProject().getDefaultBranch()), true);
		} else {
			commitId = issue.getProject().getObjectId(branch, true);
		}
		return Pair.of(commitId, branch);
	}

	private void onAiMentioned(User ai, User user, Issue issue, List<Long> participatingUserIds) {
		var branchAndCommitId = getWorkspaceCommitIdAndBranch(issue);
		String rolesInfo;		
		var assignedFields = getAssignedFields(ai, issue);
		if (!assignedFields.isEmpty()) {
			rolesInfo = "as roles [%s] ".formatted(StringUtils.join(assignedFields, ", "));
		} else {
			rolesInfo = "";
		}
		String prompt = """
			Work on issue %d %sto address %s's latest concern. \
			Do not switch checkout if the concern does not require you to write code. \
			Mention the user in your comment if you expect a response. \
			Make sure to submit work afterwards without confirmation.""".
			formatted(issue.getNumber(), rolesInfo, user.getName());
		runPrompt(ai, issue, issue.getProject(), branchAndCommitId.getLeft(),
				branchAndCommitId.getRight(), prompt, participatingUserIds);
	}

	private void onAiNotified(User ai, User user, Issue issue, List<Long> participatingUserIds) {
		var branchAndCommitId = getWorkspaceCommitIdAndBranch(issue);
		String rolesInfo;		
		var assignedFields = getAssignedFields(ai, issue);
		if (!assignedFields.isEmpty()) {
			rolesInfo = "as roles [%s] ".formatted(StringUtils.join(assignedFields, ", "));
		} else {
			rolesInfo = "";
		}
		String prompt = """
				Work on issue %d %sto check whether you are relevant to %s's latest concern. \
				Do not switch checkout if the concern does not require you to write code. \
				Respond only if you are relevant and a response is necessary. \
				Make sure to submit work afterwards without confirmation."""
				.formatted(issue.getNumber(), rolesInfo, user.getName());
		runPrompt(ai, issue, issue.getProject(), branchAndCommitId.getLeft(),
				branchAndCommitId.getRight(), prompt, participatingUserIds);
	}

	private void runPrompt(User ai, Issue issue, Project project, 
				ObjectId commitId, @Nullable String branch, String prompt,
				List<Long> eventParticipatingUserIds) {
		var participatingUserIds = new ArrayList<>(eventParticipatingUserIds);
		var awakenCount = participatingUserIds.stream().filter(ai.getId()::equals).count();
		if (awakenCount >= ai.getAiSetting().getMaxLoopCount()) {
			var message = "I reached the maximum awaken count of %d in the current event chain"
							.formatted(ai.getAiSetting().getMaxLoopCount());
			createComment(ai, issue, message);
			return;
		}
		try {
			var taskFailedCallback = newTaskFailedCallback(ai.getId(), issue.getId());
			workspaceService.runPrompt(ai, project, issue, null, commitId, branch, prompt,
					participatingUserIds, taskFailedCallback);
		} catch (Throwable t) {
			var explicitException = ExceptionUtils.find(t, ExplicitException.class);
			if (explicitException != null) {
				createComment(ai, issue, explicitException.getMessage());
			} else {
				logger.error("Error doing job via AI user", t);
				createComment(ai, issue, "Failed to do the job, check server log for details");
			}
		}		
	}

	private void createComment(User ai, Issue issue, String comment) {
		issueCommentService.create(userService.getSystem(), issue,
				"On behalf of AI user \"%s\": %s".formatted(ai.getDisplayName(), comment));
	}

	private Set<String> getAssignedFields(User ai, Issue issue) {
		var assignedFieldNames = new HashSet<String>();
		for (var field: issue.getFields()) {
			if (field.getType().equals(InputSpec.USER) && ai.getName().equals(field.getValue())) 
				assignedFieldNames.add(field.getName());
			if (field.getType().equals(InputSpec.GROUP) && field.getValue() != null) {
				var group = groupService.find(field.getValue());
				if (group != null) {
					for (var member: group.getMembers()) {
						if (member.equals(ai))
							assignedFieldNames.add(field.getName());
					}
				}
			}
		}
		return assignedFieldNames;
	}

	private void onFieldSet(User ai, Issue issue, String field, List<Long> participatingUserIds) {
		var commitIdAndBranch = getWorkspaceCommitIdAndBranch(issue);
		var prompt = """
				Work on issue %d as role '%s'. \
				Do not switch checkout if your role does not need to write code. \
				Make sure to submit work afterwards without confirmation."""
				.formatted(issue.getNumber(), field);
		runPrompt(ai, issue, issue.getProject(), commitIdAndBranch.getLeft(),
				commitIdAndBranch.getRight(), prompt, participatingUserIds);
	}

	private TaskFailedCallback newTaskFailedCallback(Long aiId, Long issueId) {
		return new TaskFailedCallback() {
		
			public void onTaskFailed(String workspaceReference) {
				createComment(userService.load(aiId), issueService.load(issueId),
						"Failed to do the job, please open workspace %s for details".formatted(workspaceReference));
			}

		};
	}

	@Transactional
	@Listen
	public void on(IssuesMoved event) {
		User user = event.getUser();
		for (var issueId: event.getIssueIds()) {
			var issue = issueService.load(issueId);

			String senderName;
			String summary;
			if (user != null) {
				senderName = user.getDisplayName();
				summary = user.getDisplayName() + " " + event.getActivity();
			} else {
				senderName = null;
				summary = StringUtils.capitalize(event.getActivity());
			}
			
			setupWatches(issue);
			
			var emojis = Emojis.getInstance();
			String replyAddress = mailService.getReplyAddress(issue);
			boolean replyable = replyAddress != null;

			Collection<String> bccEmailAddresses = new HashSet<>();
			for (IssueWatch watch : issue.getWatches()) {
				Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
				if (watch.isWatching()
						&& (visitDate == null || visitDate.before(event.getDate()))
						&& !watch.getUser().equals(user)
						&& SecurityUtils.canAccessIssue(watch.getUser().asSubject(), issue)) {
					EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified())
						bccEmailAddresses.add(emailAddress.getValue());
				}
			}

			if (!bccEmailAddresses.isEmpty()) {
				String subject = String.format(
						"[Issue %s] (%s) %s",
						issue.getReference(),
						"Moved",
						emojis.apply(issue.getTitle()));
				
				var url = urlService.urlFor(issue, true);
				String textDetail = "" +
						"Previous project: " + event.getSourceProject().getPath() + 
						"\n" +
						"Current project: " + event.getTargetProject().getPath() + 
						"\n";
				String htmlDetail = "" +
						"Previous project: " + escapeHtml5(event.getSourceProject().getPath()) +
						"<br>" +
						"Current project: " + escapeHtml5(event.getTargetProject().getPath()) + 
						"<br>";
				Unsubscribable unsubscribable = new Unsubscribable(mailService.getUnsubscribeAddress(issue));
				String htmlBody = getEmailBody(true, event, summary, htmlDetail, url, replyable, unsubscribable);
				String textBody = getEmailBody(false, event, summary, textDetail, url, replyable, unsubscribable);

				String threadingReferences = issue.getThreadingReferences();
				mailService.sendMailAsync(Sets.newHashSet(), Sets.newHashSet(),
						bccEmailAddresses, subject, htmlBody, textBody,
						replyAddress, senderName, threadingReferences);
			}
		}
	}
	
	private boolean canCreateWorkspace(User ai, Issue issue, boolean commentOnError) {
		if (!SecurityUtils.canCreateWorkspaces(ai.asSubject(), issue.getProject())) {			
			if (commentOnError) 
				createComment(ai, issue, "I need create workspace permission in this project to do the job");				
			return false;
		}
		if (issue.getProject().getDefaultBranch() == null) {
			if (commentOnError) 
				createComment(ai, issue, "I need to create workspace to do the job, but the project doesn't have code yet");				
			return false;
		}
		return true;
	}

	private boolean isAiEligible(@Nullable User user, Issue issue, User ai, boolean commentOnError) {
		if (ai.isDisabled()) {
			if (commentOnError) 
				createComment(ai, issue, "I'm disabled, and cannot do the job");				
			return false;
		} else if (user != null && user.getId() > 0) {
			if (user.isEntitledToAi(ai)) {
				return true;
			} else {
				if (commentOnError)
					createComment(ai, issue, "@%s is not entitled to interact with me".formatted(user.getName()));				
				return false;
			}
		} else {
			if (issue.getProject().isEntitledToAi(ai)) {
				return true;
			} else {
				if (commentOnError)
					createComment(ai, issue, "I'm not entitled to work on this project");				
				return false;
			}
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueNotificationManager.class);
	}

}
