package io.onedev.server.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.model.CommitQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.commit.CommitQueryUtils;

@Singleton
public class CommitNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	@Inject
	public CommitNotificationManager(MailManager mailManager, UrlManager urlManager) {
		this.mailManager = mailManager;
		this.urlManager = urlManager;
	}

	private void fillSubscribedQueryStrings(Map<User, Collection<String>> subscribedQueryStrings, User user, @Nullable NamedQuery query) {
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
			Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
			for (CommitQuerySetting setting: project.getCommitQuerySettings()) {
				for (String queryName: setting.getQuerySubscriptionSupport().getProjectQuerySubscriptions())
					fillSubscribedQueryStrings(subscribedQueryStrings, setting.getUser(), project.getSavedCommitQuery(queryName));
				for (String queryName: setting.getQuerySubscriptionSupport().getUserQuerySubscriptions()) 
					fillSubscribedQueryStrings(subscribedQueryStrings, setting.getUser(), setting.getUserQuery(queryName));
			}
			
			Collection<String> notifyEmails = new HashSet<>();
			for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				for (String queryString: entry.getValue()) {
					try {
						if (CommitQueryUtils.matches(event, user, queryString)) {
							notifyEmails.add(user.getEmail());
							break;
						}
					} catch (Exception e) {
					}
				}
			}
			
			RevCommit commit = project.getRevCommit(event.getNewCommitId(), true);
			String subject = String.format("Subscribed commit at ref '%s': %s", event.getRefName(), commit.getShortMessage());
			String url = urlManager.urlFor(project, commit);
			String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
			mailManager.sendMailAsync(notifyEmails, subject, body.toString());
		}
	}
}
