package io.onedev.server.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.Listen;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.entitymanager.IssueAuthorizationManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.event.issue.IssueChanged;
import io.onedev.server.event.issue.IssueCommented;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.infomanager.UserInfoManager;
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
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryWatchBuilder;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class IssueNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final IssueAuthorizationManager authorizationManager;
	
	private final IssueWatchManager watchManager;
	
	private final UserManager userManager;
	
	private final UserInfoManager userInfoManager;
	
	private final TransactionManager transactionManager;
	
	private final Dao dao;
	
	@Inject
	public IssueNotificationManager(MarkdownManager markdownManager, MailManager mailManager,
			IssueWatchManager watchManager, UserInfoManager userInfoManager, UserManager userManager, 
			SettingManager settingManager, IssueAuthorizationManager authorizationManager, 
			TransactionManager transactionManager, Dao dao) {
		super(markdownManager, settingManager);
		this.mailManager = mailManager;
		this.watchManager = watchManager;
		this.userInfoManager = userInfoManager;
		this.userManager = userManager;
		this.authorizationManager = authorizationManager;
		this.transactionManager = transactionManager;
		this.dao = dao;
	}
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		transactionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				IssueEvent clone = (IssueEvent) event.cloneIn(dao);
				
				Issue issue = clone.getIssue();
				User user = clone.getUser();

				String url = event.getUrl();

				String summary; 
				if (user != null)
					summary = user.getDisplayName() + " " + clone.getActivity();
				else
					summary = StringUtils.capitalize(clone.getActivity());

				for (Map.Entry<User, Boolean> entry: new QueryWatchBuilder<Issue>() {

					@Override
					protected Issue getEntity() {
						return issue;
					}

					@Override
					protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
						return issue.getProject().getIssueQueryPersonalizations();
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

				Map<String, Group> newGroups = clone.getNewGroups();
				Map<String, Collection<User>> newUsers = clone.getNewUsers();
				
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
										getHtmlBody(clone, summary, clone.getHtmlBody(), url, replyable, null), 
										getTextBody(clone, summary, clone.getTextBody(), url, replyable, null), 
										replyAddress, threadingReferences);
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
										getHtmlBody(clone, summary, clone.getHtmlBody(), url, replyable, null), 
										getTextBody(clone, summary, clone.getTextBody(), url, replyable, null), 
										replyAddress, threadingReferences);
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
				if (clone instanceof IssueCommented)
					notifiedEmailAddresses = ((IssueCommented) clone).getNotifiedEmailAddresses();
				else
					notifiedEmailAddresses = new ArrayList<>();
				
				if (clone.getRenderedMarkdown() != null) {
					for (String userName: new MentionParser().parseMentions(clone.getRenderedMarkdown())) {
						User mentionedUser = userManager.findByName(userName);
						if (mentionedUser != null) {
							watchManager.watch(issue, mentionedUser, true);
							authorizationManager.authorize(issue, mentionedUser);
							if (!isNotified(notifiedEmailAddresses, mentionedUser)) {
								String subject = String.format("[Issue %s] (Mentioned You) %s", issue.getFQN(), issue.getTitle());
								String threadingReferences = String.format("<mentioned-%s@onedev>", issue.getUUID());
								
								EmailAddress emailAddress = mentionedUser.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified()) {
									mailManager.sendMailAsync(Sets.newHashSet(emailAddress.getValue()), 
											Sets.newHashSet(), Sets.newHashSet(), subject, 
											getHtmlBody(clone, summary, clone.getHtmlBody(), url, replyable, null), 
											getTextBody(clone, summary, clone.getTextBody(), url, replyable, null),
											replyAddress, threadingReferences);
								}
								notifiedUsers.add(mentionedUser);
							}
						}
					}
				}

				if (!(clone instanceof IssueChanged) 
						|| !(((IssueChanged) clone).getChange().getData() instanceof ReferencedFromAware)) {
					Collection<String> bccEmailAddresses = new HashSet<>();
					
					for (IssueWatch watch: issue.getWatches()) {
						Date visitDate = userInfoManager.getIssueVisitDate(watch.getUser(), issue);
						if (watch.isWatching()
								&& (visitDate == null || visitDate.before(clone.getDate()))
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
								issue.getFQN(), (clone instanceof IssueOpened)?"Opened":"Updated", issue.getTitle()); 
			
						Unsubscribable unsubscribable = new Unsubscribable(mailManager.getUnsubscribeAddress(issue));
						String htmlBody = getHtmlBody(clone, summary, clone.getHtmlBody(), url, replyable, unsubscribable);
						String textBody = getTextBody(clone, summary, clone.getTextBody(), url, replyable, unsubscribable);
			
						String threadingReferences = issue.getEffectiveThreadingReference();
						mailManager.sendMailAsync(Sets.newHashSet(), Sets.newHashSet(), 
								bccEmailAddresses, subject, htmlBody, textBody, 
								replyAddress, threadingReferences);
					}
				}
			}
					
		}, LockUtils.getLock(Issue.getSerialLockName(event.getIssue().getId()), true));
		
	}
	
}
