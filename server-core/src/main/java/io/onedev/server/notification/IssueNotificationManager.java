package io.onedev.server.notification;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isNotified;
import static io.onedev.server.util.EmailAddressUtils.describe;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.io.ObjectStreamException;
import java.io.Serializable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.ai.AiTask;
import io.onedev.server.ai.ResponseHandler;
import io.onedev.server.ai.taskchecker.NoopTaskChecker;
import io.onedev.server.ai.tools.issue.GetIssue;
import io.onedev.server.ai.tools.issue.GetIssueComments;
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

		Collection<String> notifiedEmailAddresses;
		if (event instanceof IssueCommentCreated)
			notifiedEmailAddresses = ((IssueCommentCreated) event).getNotifiedEmailAddresses();
		else if (event instanceof IssueOpened)
			notifiedEmailAddresses = ((IssueOpened) event).getNotifiedEmailAddresses();
		else
			notifiedEmailAddresses = new HashSet<>();
		
		Collection<User> notifiedUsers = Sets.newHashSet();
		if (user != null) {
			if (!user.isNotifyOwnEvents() || isNotified(notifiedEmailAddresses, user))
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
				if (!member.equals(user)) {
					if (member.getType() != User.Type.AI) {
						EmailAddress emailAddress = member.getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified()) {
							mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
									Lists.newArrayList(), Lists.newArrayList(), subject, 
									getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
									getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
									replyAddress, senderName, threadingReferences);
						}
						watchService.watch(issue, member, true);
						authorizationService.authorize(issue, member);		
					} else if (isAiEntitled(issue, member)) {
						if (canCreateWorkspace(member, issue)) 
							handleFieldAssignment(member, issue, entry.getKey());
						watchService.watch(issue, member, true);
						authorizationService.authorize(issue, member);								
					} else {
						authorizationService.authorize(issue, member);								
					}
				}
			}
			
			notifiedUsers.addAll(entry.getValue().getMembers());
		}
		
		for (Map.Entry<String, Collection<User>> entry: newUsers.entrySet()) {
			String subject = String.format(
					"[Issue %s] (%s: You) %s", 
					issue.getReference(), 
					entry.getKey(), 
					emojis.apply(issue.getTitle()));
			String threadingReferences = String.format("<you-in-field-%s-%s@onedev>", entry.getKey(), issue.getUUID());
			for (User assignedUser: entry.getValue()) {
				if (!assignedUser.equals(user)) {
					if (assignedUser.getType() != User.Type.AI) {
						EmailAddress emailAddress = assignedUser.getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified()) {
							mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
									Lists.newArrayList(), Lists.newArrayList(), subject, 
									getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
									getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
									replyAddress, senderName, threadingReferences);
						}					
						watchService.watch(issue, assignedUser, true);
						authorizationService.authorize(issue, assignedUser);
					} else if (isAiEntitled(issue, assignedUser)) {
						if (canCreateWorkspace(assignedUser, issue)) 
							handleFieldAssignment(assignedUser, issue, entry.getKey());
						watchService.watch(issue, assignedUser, true);
						authorizationService.authorize(issue, assignedUser);
					} else {
						authorizationService.authorize(issue, assignedUser);
					}
				}
			}
			
			notifiedUsers.addAll(entry.getValue());
		}
		
		if (event.getCommentText() instanceof MarkdownText) {
			MarkdownText markdown = (MarkdownText) event.getCommentText();
			for (String userName: new MentionParser().parseMentions(markdown.getRendered())) {
				User mentionedUser = userService.findByName(userName);
				if (mentionedUser != null) {
					mentionService.mention(issue, mentionedUser);
					authorizationService.authorize(issue, mentionedUser);
					if (!isNotified(notifiedEmailAddresses, mentionedUser)) {						
						if (mentionedUser.getType() != User.Type.AI) {
							watchService.watch(issue, mentionedUser, true);
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
						} else if (isAiEntitled(issue, mentionedUser) 
								&& user != null 
								&& user.getId() > 0 
								&& !user.equals(mentionedUser) 
								&& canCreateWorkspace(mentionedUser, issue)) {
							watchService.watch(issue, mentionedUser, true);
							addressConcern(user, mentionedUser, issue);
						}
						notifiedUsers.add(mentionedUser);
					} else {
						watchService.watch(issue, mentionedUser, true);
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
						&& !isNotified(notifiedEmailAddresses, watch.getUser())
						&& SecurityUtils.canAccessIssue(watch.getUser().asSubject(), issue)) {
				if (watch.getUser().getType() != User.Type.AI) {
					EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified())
						bccEmailAddresses.add(emailAddress.getValue());
				} else if (event.getCommentText() instanceof MarkdownText 
						&& user != null 
						&& user.getId() > 0 
						&& !user.equals(watch.getUser())) {
					var issueId = issue.getId();
					var task = new AiTask(
						null,
						"Check issue and comments to see if you should respond to %s's latest comment. If you think you should, reply with 'yes', otherwise reply with 'no'. No any explanation should be included in your response".formatted(event.getUser().getName()),
						List.of(new GetIssue(issue.getId()), new GetIssueComments(issue.getId())),
						new NoopTaskChecker(),
						new ResponseHandler() {

							@Override
							public void onResponse(User ai, String response) {
								var issue = issueService.load(issueId);
								if (response.equalsIgnoreCase("yes") 
										&& isAiEntitled(issue, ai) 
										&& canCreateWorkspace(ai, issue)) {
									addressConcern(user, ai, issue);
								}
							}
							
						});
					userService.execute(watch.getUser(), task);
				}
			}
		}
		
		for (var notifiedUser: notifiedUsers) {
			for (var emailAddress: notifiedUser.getEmailAddresses()) {
				if (emailAddress.isVerified())
					notifiedEmailAddresses.add(emailAddress.getValue());
			}
		}
		for (var participant: issue.getExternalParticipants()) {
			if (!notifiedEmailAddresses.contains(participant.getAddress())) 
				bccEmailAddresses.add(participant.getAddress());
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

	private void addressConcern(User commenter, User ai, Issue issue) {
		try {
			String branch = issue.getBranch();									
			var assignedFields = getAssignedFields(ai, issue);
			String prompt;
			if (assignedFields.isEmpty()) {
				if (branch == null) 
					branch = Preconditions.checkNotNull(issue.getProject().getDefaultBranch());
				prompt = """
					You are %s. Address %s's concern in issue %s. \
					Examine code in workspace if necessary. \
					Do not switch branch or modify code.\
					""".formatted(ai.getName(), commenter.getName(), issue.getReference().toString(issue.getProject()));
			} else {
				if (branch == null) 
					branch = issueService.ensureBranch(ai.asSubject(), issue);
				prompt = """
					Work on current issue as roles [%s] to address %s's concern. Submit work afterwards if code is modified.
					""".formatted(StringUtils.join(assignedFields, ", "), commenter.getName());
			}
			var taskFailedCallback = newTaskFailedCallback(ai.getId(), issue.getId());
			workspaceService.runPrompt(ai, issue.getProject(), branch, prompt, taskFailedCallback);
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
		if (SecurityUtils.canAccessIssue(ai.asSubject(), issue)) {
			issueCommentService.create(ai, issue, comment);
		} else {
			issueCommentService.create(userService.getSystem(), issue, "_Commenting on behalf of **%s** as the user does not even have permission to post here._\n\n%s".formatted(ai.getName(), comment));
		}
	}

	private Set<String> getAssignedFields(User ai, Issue issue) {
		var assignedFieldNames = new HashSet<String>();
		for (var field: issue.getFields()) {
			if (field.getType().equals(InputSpec.USER) && field.getValue().equals(ai.getName())) 
				assignedFieldNames.add(field.getName());
			if (field.getType().equals(InputSpec.GROUP)) {
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

	private void handleFieldAssignment(User ai, Issue issue, String field) {
		var fieldSpec = settingService.getIssueSetting().getFieldSpec(field);
		if (fieldSpec != null) {
			if (SecurityUtils.canWriteCode(ai.asSubject(), issue.getProject())) {
				try {
					var branch = issueService.ensureBranch(ai.asSubject(), issue);
					var taskFailedCallback = newTaskFailedCallback(ai.getId(), issue.getId());
					workspaceService.runPrompt(ai, issue.getProject(), branch, "Work on current issue as role '%s'. Submit work afterwards if code is modified".formatted(field), taskFailedCallback);						
				} catch (Throwable t) {
					logger.error("Error doing job via AI user", t);
					createComment(ai, issue, "Failed to do the job, check server log for details");
				}
			} else {
				createComment(ai, issue, "I need write code permission to work as '%s' in this project".formatted(field));
			}
		} else {
			createComment(ai, issue, "I don't know how to work as '%s' since it is not defined".formatted(field));
		}
	}

	private TaskFailedCallback newTaskFailedCallback(Long aiId, Long issueId) {
		return new TaskFailedCallback() {
		
			public void onTaskFailed(String workspaceReference) {							
				createComment(userService.load(aiId), issueService.load(issueId), "Failed to do the job, please open workspace %s for details".formatted(workspaceReference));
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
	
	private boolean canCreateWorkspace(User ai, Issue issue) {
		if (!SecurityUtils.canCreateWorkspaces(ai.asSubject(), issue.getProject())) {			
			createComment(ai, issue, "I need to create workspace to do the job, but I don't have permission to create that");				
			return false;
		}
		if (issue.getProject().getDefaultBranch() == null) {
			createComment(ai, issue, "I need to create workspace to do the job, but the project doesn't have code yet");				
			return false;
		}
		return true;
	}

	private boolean isAiEntitled(Issue issue, User ai) {
		if (issue.getProject().isEntitledToAi(ai)) {
			return true;
		} else {
			createComment(ai, issue, "Sorry but this project is not entitled to access me");				
			return false;
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueNotificationManager.class);
	}

}
