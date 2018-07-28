package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.issue.IssueActionEvent;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.manager.MailManager;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.UrlManager;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.markdown.MentionParser;
import io.onedev.server.util.query.EntityQuery;
import io.onedev.server.util.query.QueryWatchBuilder;
import jersey.repackaged.com.google.common.collect.Lists;

@Singleton
public class DefaultIssueNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	private final IssueWatchManager issueWatchManager;
	
	private final UserInfoManager userInfoManager;
	
	@Inject
	public DefaultIssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager, 
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
				return issue.getProject().getSavedIssueQuery(name);
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
					else if (event instanceof IssueActionEvent)
						url = urlManager.urlFor(((IssueActionEvent)event).getAction());
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

		if (event instanceof IssueActionEvent) {
			IssueAction issueAction = ((IssueActionEvent) event).getAction();
			for (Group group: issueAction.getData().getNewGroups().values()) {
				for (User member: group.getMembers())
					watch(issue, member, true);
			}
			String url = urlManager.urlFor(issue);
			for (Map.Entry<String, User> entry: issueAction.getData().getNewUsers().entrySet()) {
				String subject = String.format("You are designated as \"%s\" of issue #%d: %s", 
						entry.getKey(), issue.getNumber(), issue.getTitle());
				String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
				mailManager.sendMailAsync(Lists.newArrayList(entry.getValue().getEmail()), subject, body.toString());
				notifiedUsers.add(entry.getValue());
			}
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
			String url = urlManager.urlFor(issue);
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
