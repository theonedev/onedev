package io.onedev.server.notification;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.cache.UserInfoManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
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
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.markdown.MentionParser;
import jersey.repackaged.com.google.common.collect.Lists;

@Singleton
public class IssueNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	private final IssueWatchManager issueWatchManager;
	
	private final UserInfoManager userInfoManager;
	
	@Inject
	public IssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager, 
			UrlManager urlManager, IssueWatchManager issueWatchManager, UserInfoManager userInfoManager) {
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.markdownManager = markdownManager;
		this.issueWatchManager = issueWatchManager;
		this.userInfoManager = userInfoManager;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		Issue issue = event.getIssue();
		User user = event.getUser();

		for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<Issue>() {

			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QuerySetting<?>> getQuerySettings() {
				return issue.getProject().getIssueQuerySettings();
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				return IssueQuery.parse(issue.getProject(), queryString, true);
			}

			@Override
			protected NamedQuery getSavedProjectQuery(String name) {
				return issue.getProject().getIssueSetting().getSavedQuery(name);
			}
			
		}.getWatches().entrySet()) {
			watch(issue, entry.getKey(), entry.getValue());
		}
		
		if (user != null)
			watch(issue, user, true);
		
		Collection<User> notifiedUsers = new HashSet<>();
		if (event instanceof MarkdownAware) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			if (markdown != null) {
				String rendered = markdownManager.render(markdown);
				Collection<User> mentionUsers = new MentionParser().parseMentions(rendered);
				if (!mentionUsers.isEmpty()) {
					for (User mentionedUser: mentionUsers)
						watch(issue, mentionedUser, true);

					String url;
					if (event instanceof IssueOpened)
						url = urlManager.urlFor(((IssueOpened)event).getIssue());
					else if (event instanceof IssueCommented) 
						url = urlManager.urlFor(((IssueCommented)event).getComment());
					else if (event instanceof IssueChangeEvent)
						url = urlManager.urlFor(((IssueChangeEvent)event).getChange());
					else 
						url = urlManager.urlFor(event.getIssue());
					
					String subject = String.format("You are mentioned in issue #%d - %s", 
							issue.getNumber(), issue.getTitle());
					String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
					
					mailManager.sendMailAsync(mentionUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
							subject, body);
					notifiedUsers.addAll(mentionUsers);
				}
			}
		} 		

		Map<String, Group> newGroups = event.getNewGroups();
		Map<String, User> newUsers = event.getNewUsers();
		
		for (Group group: newGroups.values()) {
			for (User member: group.getMembers())
				watch(issue, member, true);
		}
		String url = urlManager.urlFor(issue);
		for (Map.Entry<String, User> entry: newUsers.entrySet()) {
			String subject = String.format("You are now \"%s\" of issue #%d: %s", 
					entry.getKey(), issue.getNumber(), issue.getTitle());
			String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
			mailManager.sendMailAsync(Lists.newArrayList(entry.getValue().getEmail()), subject, body.toString());
			notifiedUsers.add(entry.getValue());
		}
				
		Collection<User> usersToNotify = new HashSet<>();
		
		for (IssueWatch watch: issue.getWatches()) {
			Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
			if (watch.isWatching() 
					&& !userInfoManager.isNotified(watch.getUser(), watch.getIssue()) 
					&& !watch.getUser().equals(event.getUser()) 
					&& (visitDate == null || visitDate.getTime()<event.getDate().getTime()) 
					&& !notifiedUsers.contains(watch.getUser())) {
				usersToNotify.add(watch.getUser());
				userInfoManager.setIssueNotified(watch.getUser(), watch.getIssue(), true);
				issueWatchManager.save(watch);
			}
		}

		if (!usersToNotify.isEmpty()) {
			String subject = String.format("New activities in issue #%d - %s", issue.getNumber(), issue.getTitle());
			String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
			mailManager.sendMailAsync(usersToNotify.stream().map(User::getEmail).collect(Collectors.toList()), subject, body);
		}			
	}
	
	private void watch(Issue issue, User user, boolean watching) {
		IssueWatch watch = (IssueWatch) issue.getWatch(user, true);
		if (watch.isNew()) {
			watch.setWatching(watching);
			issueWatchManager.save(watch);
		}
	}

}
