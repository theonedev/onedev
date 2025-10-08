package io.onedev.server.notification;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.CommitQueryPersonalization;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.web.UrlService;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;

@Singleton
public class CommitNotificationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(CommitNotificationManager.class);

	@Inject
	private MailService mailService;

	@Inject
	private UrlService urlService;

	private void fillSubscribedQueryStrings(Map<User, Collection<String>> subscribedQueryStrings, 
			User user, @Nullable NamedQuery query) {
		if (query != null) {
			Collection<String> value = subscribedQueryStrings.get(user);
			if (value == null) {
				value = new HashSet<>();
				subscribedQueryStrings.put(user, value);
			}
			value.add(query.getQuery());
		}
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId())) {
			Project project = event.getProject();
			RevCommit commit = project.getRevCommit(event.getNewCommitId(), false);
			if (commit != null) {
				Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
				for (CommitQueryPersonalization personalization: project.getCommitQueryPersonalizations()) {
					var user = personalization.getUser();
					for (String name : personalization.getQuerySubscriptionSupport().getQuerySubscriptions()) {
						String globalName = NamedQuery.getCommonName(name);
						if (globalName != null) {
							fillSubscribedQueryStrings(subscribedQueryStrings, user,
									NamedQuery.find(project.getNamedCommitQueries(), globalName));
						}
						String personalName = NamedQuery.getPersonalName(name);
						if (personalName != null) {
							fillSubscribedQueryStrings(subscribedQueryStrings, user,
									NamedQuery.find(personalization.getQueries(), personalName));
						}
					}
				}
				
				Collection<String> notifyEmails = new HashSet<>();
				for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
					User user = entry.getKey();
					if (user.asSubject().isPermitted(new ProjectPermission(event.getProject(), new ReadCode()))) {
						for (String queryString: entry.getValue()) {
							User.push(user);
							try {
								if (CommitQuery.parse(project, queryString, true).matches(event)) {
									EmailAddress emailAddress = user.getPrimaryEmailAddress();
									if (emailAddress != null && emailAddress.isVerified())
										notifyEmails.add(emailAddress.getValue());
									break;
								}
							} catch (Exception e) {
								String message = String.format("Error processing commit subscription "
										+ "(user: %s, project: %s, commit: %s, query: %s)", 
										user.getName(), project.getPath(), event.getNewCommitId().name(), queryString);
								logger.error(message, e);
							} finally {
								User.pop();
							}
						}
					}
				}
				
				String subject = String.format(
						"[Commit %s:%s] (%s) %s", 
						project.getPath(), 
						GitUtils.abbreviateSHA(commit.name()), 
						commit.getShortMessage(),
						StringUtils.capitalize(event.getActivity()));

				String url = urlService.urlFor(project, commit, true);
				String summary = String.format("Commit authored by %s", commit.getAuthorIdent().getName());

				String threadingReferences = "<commit-" + commit.name() + "@onedev>";
				
				if (!notifyEmails.isEmpty()) {
					mailService.sendMailAsync(Lists.newArrayList(), Lists.newArrayList(), notifyEmails, subject,
							getEmailBody(true, event, summary, event.getHtmlBody(), url, false, null),
							getEmailBody(false, event, summary, event.getTextBody(), url, false, null),
							null, commit.getAuthorIdent().getName(), threadingReferences);
				}
			}
		}
	}
}
