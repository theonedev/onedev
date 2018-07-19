package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

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
	
	@Inject
	public DefaultIssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager, 
			UrlManager urlManager, IssueWatchManager issueWatchManager) {
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.markdownManager = markdownManager;
		this.issueWatchManager = issueWatchManager;
	}
	
	private String getUrl(IssueEvent event) {
		if (event instanceof IssueOpened)
			return urlManager.urlFor(((IssueOpened)event).getIssue());
		else if (event instanceof IssueCommented) 
			return urlManager.urlFor(((IssueCommented)event).getComment());
		else if (event instanceof IssueActionEvent)
			return urlManager.urlFor(((IssueActionEvent)event).getAction());
		else 
			return urlManager.urlFor(event.getIssue());
	}

	private void appendParagraph(StringBuilder builder) {
		builder.append("<p style='margin: 16px 0;'>");
	}
	
	private void appendIssueInfo(StringBuilder builder, IssueEvent event) {
		Issue issue = event.getIssue();
		builder.append("<div style='border: 1px solid #E0E0E0; background: #F9F9F9; padding: 16px; border-radius: 4px;'>");
		builder.append(String.format("<a href='%s'>Issue #%d: %s</a>", 
				getUrl(event), issue.getNumber(), issue.getTitle()));
		if (event.getIssue().getDescription() != null) {
			appendParagraph(builder);
			builder.append(markdownManager.escape(event.getIssue().getDescription()));
		}
		builder.append("</div>");
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		Collection<User> involvedUsers = new HashSet<>();
		Collection<User> notifiedUsers = new HashSet<>();
		
		involvedUsers.add(event.getUser());

		Issue issue = event.getIssue();
		
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
		
		if (event instanceof IssueActionEvent) {
			IssueAction issueAction = ((IssueActionEvent) event).getAction();
			for (Group group: issueAction.getData().getNewGroups().values()) 
				involvedUsers.addAll(group.getMembers());
			for (Map.Entry<String, User> entry: issueAction.getData().getNewUsers().entrySet()) {
				String subject = String.format("You are designated as \"%s\" of issue #%d: %s", 
						entry.getKey(), issue.getNumber(), issue.getTitle());
				StringBuilder body = new StringBuilder();
				body.append(event.describeAsHtml());
				appendParagraph(body);
				appendIssueInfo(body, event);
				mailManager.sendMailAsync(Lists.newArrayList(entry.getValue().getEmail()), subject, body.toString());
				notifiedUsers.add(entry.getValue());
			}
		}
		
		if (event instanceof MarkdownAware) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			if (markdown != null) {
				String rendered = markdownManager.render(markdown);
				Collection<User> mentionedUsers = new MentionParser().parseMentions(rendered);
				mentionedUsers.removeAll(notifiedUsers);
				for (User user: mentionedUsers) {
					String subject = String.format("You are mentioned in issue #%d: %s", 
							issue.getNumber(), issue.getTitle());
					StringBuilder body = new StringBuilder();
					body.append(event.describeAsHtml());
					appendParagraph(body);
					appendIssueInfo(body, event);
					mailManager.sendMailAsync(Lists.newArrayList(user.getEmail()), subject, body.toString());
					notifiedUsers.add(user);
				}
			}
		} 		

		involvedUsers.addAll(notifiedUsers);
		for (User user: involvedUsers)
			watch(issue, user, true);
		
		Collection<User> usersToNotify = new HashSet<>();
		for (IssueWatch watch: issue.getWatches()) {
			if (watch.isWatching() && !watch.getUser().equals(event.getUser()) 
					&& !notifiedUsers.contains(watch.getUser())) {  
				usersToNotify.add(watch.getUser());
			}
		}
		
		for (User user: usersToNotify) {
			StringBuilder body = new StringBuilder();
			body.append(event.describeAsHtml());
			appendParagraph(body);
			appendIssueInfo(body, event);
			mailManager.sendMailAsync(Lists.newArrayList(user.getEmail()), event.getTitle(), body.toString());
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
