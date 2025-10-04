package io.onedev.server.notification;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.server.service.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.project.issue.IssuesMoved;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.*;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.web.UrlService;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.xodus.VisitInfoService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static io.onedev.server.notification.NotificationUtils.isNotified;
import static io.onedev.server.util.EmailAddressUtils.describe;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

@Singleton
public class IssueNotificationManager {

	@Inject
	private MailService mailService;

	@Inject
	private IssueAuthorizationService authorizationService;

	@Inject
	private IssueWatchService watchService;

	@Inject
	private UserService userService;

	@Inject
	private VisitInfoService userInfoManager;

	@Inject
	private IssueMentionService mentionService;

	@Inject
	private IssueQueryPersonalizationService queryPersonalizationService;

	@Inject
	private IssueService issueService;

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
			if (!user.isSystem() && !user.isServiceAccount())
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
					EmailAddress emailAddress = member.getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
								Lists.newArrayList(), Lists.newArrayList(), subject, 
								getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
								getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
								replyAddress, senderName, threadingReferences);
					}
				}
			}
			
			for (User member: entry.getValue().getMembers()) {
				if (!member.isServiceAccount()) 
					watchService.watch(issue, member, true);
				authorizationService.authorize(issue, member);
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
			for (User member: entry.getValue()) {
				if (!member.equals(user)) {
					EmailAddress emailAddress = member.getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailService.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
								Lists.newArrayList(), Lists.newArrayList(), subject, 
								getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
								getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
								replyAddress, senderName, threadingReferences);
					}					
				}
			}
			
			for (User each: entry.getValue()) {
				if (!each.isServiceAccount()) 
					watchService.watch(issue, each, true);
				authorizationService.authorize(issue, each);
			}
			notifiedUsers.addAll(entry.getValue());
		}
		
		if (event.getCommentText() instanceof MarkdownText) {
			MarkdownText markdown = (MarkdownText) event.getCommentText();
			for (String userName: new MentionParser().parseMentions(markdown.getRendered())) {
				User mentionedUser = userService.findByName(userName);
				if (mentionedUser != null) {
					mentionService.mention(issue, mentionedUser);
					if (!mentionedUser.isServiceAccount())
						watchService.watch(issue, mentionedUser, true);
					authorizationService.authorize(issue, mentionedUser);
					if (!isNotified(notifiedEmailAddresses, mentionedUser)) {
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
						notifiedUsers.add(mentionedUser);
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
				EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
				if (emailAddress != null && emailAddress.isVerified())
					bccEmailAddresses.add(emailAddress.getValue());
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
	
}
