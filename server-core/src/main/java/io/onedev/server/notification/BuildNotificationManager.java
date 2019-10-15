package io.onedev.server.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.entity.build.BuildQuery;

@Singleton
public class BuildNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	@Inject
	public BuildNotificationManager(MailManager mailManager, UrlManager urlManager) {
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
	
	public void notify(Build build, Collection<String> emails) {
		String subject;
		if (build.getVersion() != null) {
			subject = String.format("Build %s/%s/#%s (%s) is %s", build.getProject().getName(), build.getJobName(), 
					build.getNumber(), build.getVersion(), build.getStatus().getDisplayName().toLowerCase());
		} else {
			subject = String.format("Build %s/%s/#%s is %s", build.getProject().getName(), build.getJobName(), 
					build.getNumber(), build.getStatus().getDisplayName().toLowerCase());
		}
		String url = urlManager.urlFor(build);
		String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
		mailManager.sendMailAsync(emails, subject, body.toString());
	}
	
	@Sessional
	@Listen
	public void on(BuildEvent event) {
		Project project = event.getProject();
		Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
		for (BuildQuerySetting setting: project.getBuildQuerySettings()) {
			for (String queryName: setting.getQuerySubscriptionSupport().getProjectQuerySubscriptions())
				fillSubscribedQueryStrings(subscribedQueryStrings, setting.getUser(), project.getSavedBuildQuery(queryName));
			for (String queryName: setting.getQuerySubscriptionSupport().getUserQuerySubscriptions()) 
				fillSubscribedQueryStrings(subscribedQueryStrings, setting.getUser(), setting.getUserQuery(queryName));
		}

		Build build = event.getBuild();
		Collection<String> notifyEmails = new HashSet<>();
		for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
			User user = entry.getKey();
			for (String queryString: entry.getValue()) {
				try {
					if (BuildQuery.parse(event.getProject(), queryString).matches(build, user)) {
						notifyEmails.add(user.getEmail());
						break;
					}
				} catch (Exception e) {
				}
			}
		}
		notify(build, notifyEmails);
	}
	
}
