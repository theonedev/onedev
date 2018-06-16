package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.OneDev;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.issue.IssueChanged;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueNotificationManager;
import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.manager.MailManager;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.UrlManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.NamedQuery;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.markdown.MentionParser;
import io.onedev.server.util.readcallback.ReadCallback;
import io.onedev.server.util.readcallback.ReadCallbackServlet;
import jersey.repackaged.com.google.common.collect.Lists;

@Singleton
public class DefaultIssueNotificationManager implements IssueNotificationManager {
	
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
		else if (event instanceof IssueChanged)
			return urlManager.urlFor(((IssueChanged)event).getChange());
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
	
	private void appendReadCallback(StringBuilder builder, User user, IssueEvent event) {
		EmailReadCallback callback = new EmailReadCallback(user.getId(), event.getIssue().getId(), event.getDate().getTime()+1000L);
		builder.append("<img src='").append(ReadCallbackServlet.getUrl(callback)).append("' alt='read track'>");
	}
	
	private boolean matches(Map<String, Optional<IssueQuery>> parsedQueries, Issue issue, @Nullable NamedQuery namedQuery) {
		if (namedQuery != null) {
			Optional<IssueQuery> issueQuery = parsedQueries.get(namedQuery.getQuery());
			if (issueQuery == null) {
				try {
					issueQuery = Optional.of(IssueQuery.parse(issue.getProject(), namedQuery.getQuery(), true));
				} catch (Exception e) {
					issueQuery = Optional.empty();
				}
				parsedQueries.put(namedQuery.getQuery(), issueQuery);
			}
			return issueQuery.isPresent() && issueQuery.get().matches(issue); 
		} else {
			return false;
		}
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		Collection<User> involvedUsers = new HashSet<>();
		Collection<User> notifiedUsers = new HashSet<>();
		
		involvedUsers.add(event.getUser());

		Issue issue = event.getIssue();
		
		OneDev.getInstance(VisitManager.class).visitIssue(event.getUser(), issue);
		
		Map<String, Optional<IssueQuery>> parsedQueries = new HashMap<>();
		
		for (IssueQuerySetting setting: issue.getProject().getIssueQuerySettings()) {
			boolean watched = false;
			for (Map.Entry<String, Boolean> entry: setting.getUserQueryWatches().entrySet()) {
				if (matches(parsedQueries, issue, setting.getUserQuery(entry.getKey()))) {
					watch(issue, setting.getUser(), entry.getValue());
					watched = true;
					break;
				}
			}
			if (!watched) {
				for (Map.Entry<String, Boolean> entry: setting.getProjectQueryWatches().entrySet()) {
					if (matches(parsedQueries, issue, issue.getProject().getSavedIssueQuery(entry.getKey()))) {
						watch(issue, setting.getUser(), entry.getValue());
						watched = true;
						break;
					}
				}
			}
		}
		
		if (event instanceof IssueChanged) {
			IssueChange issueChange = ((IssueChanged) event).getChange();
			for (Group group: issueChange.getData().getNewGroups().values()) 
				involvedUsers.addAll(group.getMembers());
			for (Map.Entry<String, User> entry: issueChange.getData().getNewUsers().entrySet()) {
				String subject = String.format("You are designated as \"%s\" of issue #%d: %s", 
						entry.getKey(), issue.getNumber(), issue.getTitle());
				StringBuilder body = new StringBuilder();
				body.append(event.describeAsHtml());
				appendParagraph(body);
				appendIssueInfo(body, event);
				appendReadCallback(body, entry.getValue(), event);
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
					appendReadCallback(body, user, event);
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
			appendReadCallback(body, user, event);
			mailManager.sendMailAsync(Lists.newArrayList(user.getEmail()), event.getTitle(), body.toString());
		}
	}
	
	private void watch(Issue issue, User user, boolean watching) {
		IssueWatch watch = issue.getWatch(user);
		if (watch == null) {
			watch = new IssueWatch();
			watch.setIssue(issue);
			watch.setUser(user);
			watch.setWatching(watching);
			issue.getWatches().add(watch);
			issueWatchManager.save(watch);
		}
	}

	public static class EmailReadCallback implements ReadCallback {

		private Long userId;
		
		private Long issueId;
		
		private Long eventTime;
		
		public EmailReadCallback() {
		}
		
		public EmailReadCallback(Long userId, Long issueId, Long eventTime) {
			this.userId = userId;
			this.issueId = issueId;
			this.eventTime = eventTime;
		}
		
		@Override
		public void onRead() {
			User user = OneDev.getInstance(UserManager.class).get(userId);
			Issue issue = OneDev.getInstance(IssueManager.class).get(issueId);
			if (user != null && issue != null) 
				OneDev.getInstance(VisitManager.class).visitIssue(user, issue, new Date(eventTime));
		}
		
	}
}
