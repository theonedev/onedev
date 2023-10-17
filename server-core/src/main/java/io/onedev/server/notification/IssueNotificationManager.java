package io.onedev.server.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.*;
import io.onedev.server.event.project.issue.*;
import io.onedev.server.util.ProjectScope;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.server.event.Listen;
import io.onedev.server.xodus.VisitInfoManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
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
import io.onedev.server.util.commenttext.MarkdownText;

@Singleton
public class IssueNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final IssueAuthorizationManager authorizationManager;
	
	private final IssueWatchManager watchManager;
	
	private final UserManager userManager;
	
	private final VisitInfoManager userInfoManager;
	
	private final IssueMentionManager mentionManager;
	
	private final IssueQueryPersonalizationManager queryPersonalizationManager;
	
	@Inject
	public IssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager, 
									IssueWatchManager watchManager, VisitInfoManager userInfoManager, 
									UserManager userManager, SettingManager settingManager, 
									IssueAuthorizationManager authorizationManager, IssueMentionManager mentionManager, 
									IssueQueryPersonalizationManager queryPersonalizationManager) {
		super(markdownManager, settingManager);
		
		this.mailManager = mailManager;
		this.watchManager = watchManager;
		this.userInfoManager = userInfoManager;
		this.userManager = userManager;
		this.authorizationManager = authorizationManager;
		this.mentionManager = mentionManager;
		this.queryPersonalizationManager = queryPersonalizationManager;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		if (event.isMinor())
			return;
		
		Issue issue = event.getIssue();
		User user = event.getUser();

		String url = event.getUrl();

		String senderName;
		String summary; 
		if (user != null) {
			senderName = user.getDisplayName();
			summary = user.getDisplayName() + " " + event.getActivity();
		} else {
			senderName = null;
			summary = StringUtils.capitalize(event.getActivity());
		}

		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return queryPersonalizationManager.query(new ProjectScope(issue.getProject(), true, true));
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
			if (SecurityUtils.canAccess(entry.getKey().asSubject(), issue))
				watchManager.watch(issue, entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return userManager.query().stream().map(it->it.getIssueQueryPersonalization()).collect(Collectors.toList());
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				IssueQueryParseOption option = new IssueQueryParseOption().withCurrentBuildCriteria(true);
				return IssueQuery.parse(null, queryString, option, true);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return settingManager.getIssueSetting().getNamedQueries();
			}
			
		}.getWatches().entrySet()) {
			if (SecurityUtils.canAccess(entry.getKey().asSubject(), issue))
				watchManager.watch(issue, entry.getKey(), entry.getValue());
		}
		
		Collection<User> notifiedUsers = Sets.newHashSet();
		if (user != null) {
			notifiedUsers.add(user); // no need to notify the user generating the event
			if (!user.isSystem())
				watchManager.watch(issue, user, true);
		}

		Map<String, Group> newGroups = event.getNewGroups();
		Map<String, Collection<User>> newUsers = event.getNewUsers();
		
		String replyAddress = mailManager.getReplyAddress(issue);
		boolean replyable = replyAddress != null;
		for (Map.Entry<String, Group> entry: newGroups.entrySet()) {
			String subject = String.format("[Issue %s] (%s: You) %s", issue.getFQN(), entry.getKey(), issue.getTitle());
			String threadingReferences = String.format("<you-in-field-%s-%s@onedev>", entry.getKey(), issue.getUUID());
			for (User member: entry.getValue().getMembers()) {
				if (!member.equals(user)) {
					EmailAddress emailAddress = member.getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailManager.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
								Lists.newArrayList(), Lists.newArrayList(), subject, 
								getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
								getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
								replyAddress, senderName, threadingReferences);
					}
				}
			}
			
			for (User member: entry.getValue().getMembers()) {
				watchManager.watch(issue, member, true);
				authorizationManager.authorize(issue, member);
			}
			
			notifiedUsers.addAll(entry.getValue().getMembers());
		}
		
		for (Map.Entry<String, Collection<User>> entry: newUsers.entrySet()) {
			String subject = String.format("[Issue %s] (%s: You) %s", issue.getFQN(), entry.getKey(), issue.getTitle());
			String threadingReferences = String.format("<you-in-field-%s-%s@onedev>", entry.getKey(), issue.getUUID());
			for (User member: entry.getValue()) {
				if (!member.equals(user)) {
					EmailAddress emailAddress = member.getPrimaryEmailAddress();
					if (emailAddress != null && emailAddress.isVerified()) {
						mailManager.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
								Lists.newArrayList(), Lists.newArrayList(), subject, 
								getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, null), 
								getEmailBody(false, event, summary, event.getTextBody(), url, replyable, null), 
								replyAddress, senderName, threadingReferences);
					}					
				}
			}
			
			for (User each: entry.getValue()) {
				watchManager.watch(issue, each, true);
				authorizationManager.authorize(issue, each);
			}
			notifiedUsers.addAll(entry.getValue());
		}
		
		Collection<String> notifiedEmailAddresses;
		if (event instanceof IssueCommentCreated)
			notifiedEmailAddresses = ((IssueCommentCreated) event).getNotifiedEmailAddresses();
		else
			notifiedEmailAddresses = new ArrayList<>();
		
		if (event.getCommentText() instanceof MarkdownText) {
			MarkdownText markdown = (MarkdownText) event.getCommentText();
			for (String userName: new MentionParser().parseMentions(markdown.getRendered())) {
				User mentionedUser = userManager.findByName(userName);
				if (mentionedUser != null) {
					mentionManager.mention(issue, mentionedUser);
					watchManager.watch(issue, mentionedUser, true);
					authorizationManager.authorize(issue, mentionedUser);
					if (!isNotified(notifiedEmailAddresses, mentionedUser)) {
						String subject = String.format("[Issue %s] (Mentioned You) %s", issue.getFQN(), issue.getTitle());
						String threadingReferences = String.format("<mentioned-%s@onedev>", issue.getUUID());
						
						EmailAddress emailAddress = mentionedUser.getPrimaryEmailAddress();
						if (emailAddress != null && emailAddress.isVerified()) {
							mailManager.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
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
		
		for (IssueWatch watch: issue.getWatches()) {
			Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
			if (watch.isWatching()
					&& (visitDate == null || visitDate.before(event.getDate()))
					&& !notifiedUsers.contains(watch.getUser())
					&& !isNotified(notifiedEmailAddresses, watch.getUser())
					&& SecurityUtils.canAccess(watch.getUser().asSubject(), issue)) {
				EmailAddress emailAddress = watch.getUser().getPrimaryEmailAddress();
				if (emailAddress != null && emailAddress.isVerified())
					bccEmailAddresses.add(emailAddress.getValue());
			}
		}

		if (!bccEmailAddresses.isEmpty()) {
			String subject = String.format("[Issue %s] (%s) %s", 
					issue.getFQN(), (event instanceof IssueOpened)?"Opened":"Updated", issue.getTitle()); 

			Unsubscribable unsubscribable = new Unsubscribable(mailManager.getUnsubscribeAddress(issue));
			String htmlBody = getEmailBody(true, event, summary, event.getHtmlBody(), url, replyable, unsubscribable);
			String textBody = getEmailBody(false, event, summary, event.getTextBody(), url, replyable, unsubscribable);

			String threadingReferences = issue.getEffectiveThreadingReference();
			mailManager.sendMailAsync(Sets.newHashSet(), Sets.newHashSet(), 
					bccEmailAddresses, subject, htmlBody, textBody, 
					replyAddress, senderName, threadingReferences);
		}
	}
	
}
