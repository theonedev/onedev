package io.onedev.server.notification;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.issue.changedata.IssueChangeData;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.markdown.MentionParser;

@Singleton
public class IssueNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final MarkdownManager markdownManager;
	
	private final IssueWatchManager issueWatchManager;
	
	private final UserManager userManager;
	
	private final UserInfoManager userInfoManager;
	
	private final SettingManager settingManager;
	
	@Inject
	public IssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager,
			UrlManager urlManager, IssueWatchManager issueWatchManager, UserInfoManager userInfoManager,
			UserManager userManager, SettingManager settingManager) {
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.markdownManager = markdownManager;
		this.issueWatchManager = issueWatchManager;
		this.userInfoManager = userInfoManager;
		this.userManager = userManager;
		this.settingManager = settingManager;
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
		
		for (Map.Entry<String, Group> entry: newGroups.entrySet()) {
			String subject = String.format("You are now \"%s\" of issue %s", entry.getKey(), issue.getNumberAndTitle());
			Set<String> emails = entry.getValue().getMembers()
					.stream()
					.filter(it->!it.equals(user))
					.map(it->it.getEmail())
					.collect(Collectors.toSet());
			mailManager.sendMailAsync(emails, subject, getHtmlBody(event, url), getTextBody(event, url));
			
			for (User member: entry.getValue().getMembers())
				issueWatchManager.watch(issue, member, true);
			
			notifiedUsers.addAll(entry.getValue().getMembers());
		}
		for (Map.Entry<String, Collection<User>> entry: newUsers.entrySet()) {
			String subject = String.format("You are now \"%s\" of issue %s", entry.getKey(), issue.getNumberAndTitle());
			Set<String> emails = entry.getValue()
					.stream()
					.filter(it->!it.equals(user))
					.map(it->it.getEmail())
					.collect(Collectors.toSet());
			mailManager.sendMailAsync(emails, subject, getHtmlBody(event, url), getTextBody(event, url));
			
			for (User each: entry.getValue())
				issueWatchManager.watch(issue, each, true);
			notifiedUsers.addAll(entry.getValue());
		}
		
		if (event instanceof MarkdownAware) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			if (markdown != null) {
				String rendered = markdownManager.render(markdown);
				
				for (String userName: new MentionParser().parseMentions(rendered)) {
					User mentionedUser = userManager.findByName(userName);
					if (mentionedUser != null) {
						String subject = String.format("You are mentioned in issue %s", issue.getNumberAndTitle());
						mailManager.sendMailAsync(Sets.newHashSet(mentionedUser.getEmail()),
								subject, getHtmlBody(event, url), getTextBody(event, url));
						
						issueWatchManager.watch(issue, mentionedUser, true);
						notifiedUsers.add(mentionedUser);
					}
				}
			}
		}
		
		boolean notifyWatchers = false;
		if (event instanceof IssueChangeEvent) {
			IssueChangeData changeData = ((IssueChangeEvent) event).getChange().getData();
			if (!(changeData instanceof IssueReferencedFromCodeCommentData
					|| changeData instanceof IssueReferencedFromIssueData
					|| changeData instanceof IssueReferencedFromPullRequestData
					|| changeData instanceof IssueDescriptionChangeData)) {
				notifyWatchers = true;
			}
		} else {
			notifyWatchers = true;
		}
		
		if (notifyWatchers) {
			Collection<User> usersToNotify = new HashSet<>();
			
			for (IssueWatch watch: issue.getWatches()) {
				Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
				if (watch.isWatching()
						&& (visitDate == null || visitDate.before(event.getDate()))
						&& !notifiedUsers.contains(watch.getUser())) {
					usersToNotify.add(watch.getUser());
				}
			}

			if (!usersToNotify.isEmpty()) {
				String subject;
				if (user != null)
					subject = String.format("%s %s", user.getDisplayName(), event.getActivity(true));
				else
					subject = event.getActivity(true);

				mailManager.sendMailAsync(usersToNotify.stream().map(User::getEmail).collect(Collectors.toList()),
						subject, getHtmlBody(event, url), getTextBody(event, url));
			}
		}
	}
}
