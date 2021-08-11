package io.onedev.server.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.issue.IssueQuery;

@Singleton
public class IssueNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final IssueWatchManager issueWatchManager;
	
	private final UserManager userManager;
	
	private final UserInfoManager userInfoManager;
	
	@Inject
	public IssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager,
			UrlManager urlManager, IssueWatchManager issueWatchManager, UserInfoManager userInfoManager,
			UserManager userManager, SettingManager settingManager) {
		super(markdownManager, settingManager);
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.issueWatchManager = issueWatchManager;
		this.userInfoManager = userInfoManager;
		this.userManager = userManager;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		Issue issue = event.getIssue();
		User user = event.getUser();

		String url;
		if (event instanceof IssueCommented)
			url = urlManager.urlFor(((IssueCommented)event).getComment());
		else if (event instanceof IssueChangeEvent)
			url = urlManager.urlFor(((IssueChangeEvent)event).getChange());
		else
			url = urlManager.urlFor(issue);

		String summary = issue.getState() + " - "; 
		if (user != null)
			summary = summary + user.getDisplayName() + " " + event.getActivity();
		else
			summary = summary + event.getActivity();

		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QuerySetting<?>> getQuerySettings() {
				return issue.getProject().getUserIssueQuerySettings();
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				return IssueQuery.parse(issue.getProject(), queryString, true, true, false, false, false);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return issue.getProject().getIssueSetting().getNamedQueries(true);
			}
			
		}.getWatches().entrySet()) {
			issueWatchManager.watch(issue, entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QuerySetting<?>> getQuerySettings() {
				return userManager.query().stream().map(it->it.getIssueQuerySetting()).collect(Collectors.toList());
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				return IssueQuery.parse(null, queryString, true, true, false, false, false);
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return settingManager.getIssueSetting().getNamedQueries();
			}
			
		}.getWatches().entrySet()) {
			issueWatchManager.watch(issue, entry.getKey(), entry.getValue());
		}
		
		Collection<User> notifiedUsers = Sets.newHashSet();
		if (user != null) {
			notifiedUsers.add(user); // no need to notify the user generating the event
			if (!user.isSystem())
				issueWatchManager.watch(issue, user, true);
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
					mailManager.sendMailAsync(Sets.newHashSet(member.getEmail()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
							getTextBody(event, summary, event.getTextBody(), url, replyable, null), 
							replyAddress, threadingReferences);
				}
			}
			
			for (User member: entry.getValue().getMembers())
				issueWatchManager.watch(issue, member, true);
			
			notifiedUsers.addAll(entry.getValue().getMembers());
		}
		
		for (Map.Entry<String, Collection<User>> entry: newUsers.entrySet()) {
			String subject = String.format("[Issue %s] (%s: You) %s", issue.getFQN(), entry.getKey(), issue.getTitle());
			String threadingReferences = String.format("<you-in-field-%s-%s@onedev>", entry.getKey(), issue.getUUID());
			for (User member: entry.getValue()) {
				if (!member.equals(user)) {
					mailManager.sendMailAsync(Sets.newHashSet(member.getEmail()), 
							Lists.newArrayList(), Lists.newArrayList(), subject, 
							getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
							getTextBody(event, summary, event.getTextBody(), url, replyable, null), 
							replyAddress, threadingReferences);
				}
			}
			
			for (User each: entry.getValue())
				issueWatchManager.watch(issue, each, true);
			notifiedUsers.addAll(entry.getValue());
		}
		
		Collection<String> notifiedEmailAddresses;
		if (event instanceof IssueCommented)
			notifiedEmailAddresses = ((IssueCommented) event).getNotifiedEmailAddresses();
		else
			notifiedEmailAddresses = new ArrayList<>();
		
		if (event.getRenderedMarkdown() != null) {
			for (String userName: new MentionParser().parseMentions(event.getRenderedMarkdown())) {
				User mentionedUser = userManager.findByName(userName);
				if (mentionedUser != null) {
					issueWatchManager.watch(issue, mentionedUser, true);
					if (!notifiedEmailAddresses.stream().anyMatch(mentionedUser.getEmails()::contains)) {
						String subject = String.format("[Issue %s] (Mentioned You) %s", issue.getFQN(), issue.getTitle());
						String threadingReferences = String.format("<mentioned-%s@onedev>", issue.getUUID());
						
						mailManager.sendMailAsync(Sets.newHashSet(mentionedUser.getEmail()), 
								Sets.newHashSet(), Sets.newHashSet(), subject, 
								getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, null), 
								getTextBody(event, summary, event.getTextBody(), url, replyable, null),
								replyAddress, threadingReferences);
						notifiedUsers.add(mentionedUser);
					}
				}
			}
		}

		if (!(event instanceof IssueChangeEvent) 
				|| !(((IssueChangeEvent) event).getChange().getData() instanceof ReferencedFromAware)) {
			Collection<User> bccUsers = new HashSet<>();
			
			for (IssueWatch watch: issue.getWatches()) {
				Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
				if (watch.isWatching()
						&& (visitDate == null || visitDate.before(event.getDate()))
						&& !notifiedUsers.contains(watch.getUser())
						&& !notifiedEmailAddresses.stream().anyMatch(watch.getUser().getEmails()::contains)) {
					bccUsers.add(watch.getUser());
				}
			}
	
			if (!bccUsers.isEmpty()) {
				String subject = String.format("[Issue %s] (Updated) %s", issue.getFQN(), issue.getTitle()); 
	
				Unsubscribable unsubscribable = new Unsubscribable(mailManager.getUnsubscribeAddress(issue));
				String htmlBody = getHtmlBody(event, summary, event.getHtmlBody(), url, replyable, unsubscribable);
				String textBody = getTextBody(event, summary, event.getTextBody(), url, replyable, unsubscribable);
	
				String threadingReferences = issue.getEffectiveThreadingReference();
				mailManager.sendMailAsync(Sets.newHashSet(), Sets.newHashSet(), 
						bccUsers.stream().map(User::getEmail).collect(Collectors.toList()),
						subject, htmlBody, textBody, replyAddress, threadingReferences);
			}
		}
	}

}
