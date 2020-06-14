package io.onedev.server.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.entity.build.BuildQuery;

@Singleton
public class BuildNotificationManager extends AbstractNotificationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(BuildNotificationManager.class);
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final UserManager userManager;
	
	private final SettingManager settingManager;
	
	@Inject
	public BuildNotificationManager(MailManager mailManager, UrlManager urlManager, 
			UserManager userManager, SettingManager settingManager) {
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.userManager = userManager;
		this.settingManager = settingManager;
	}

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
		mailManager.sendMailAsync(emails, subject, getHtmlBody(null, url), getTextBody(null, url));
	}
	
	@Sessional
	@Listen
	public void on(BuildEvent event) {
		Project project = event.getProject();
		Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
		for (BuildQuerySetting setting: project.getUserBuildQuerySettings()) {
			for (String name: setting.getQuerySubscriptionSupport().getQuerySubscriptions()) {
				fillSubscribedQueryStrings(subscribedQueryStrings, setting.getUser(), 
						NamedQuery.find(project.getBuildSetting().getNamedQueries(true), name));
			}
			for (String name: setting.getQuerySubscriptionSupport().getUserQuerySubscriptions()) { 
				fillSubscribedQueryStrings(subscribedQueryStrings, setting.getUser(), 
						NamedQuery.find(setting.getUserQueries(), name));
			}
		}

		Build build = event.getBuild();
		Collection<String> notifyEmails = new HashSet<>();
		for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
			User user = entry.getKey();
			for (String queryString: entry.getValue()) {
				User.push(user);
				try {
					if (BuildQuery.parse(event.getProject(), queryString, true, true).matches(build)) {
						notifyEmails.add(user.getEmail());
						break;
					}
				} catch (Exception e) {
					String message = String.format("Error processing build subscription (user: %s, build: %s, query: %s)", 
							user.getName(), build.getFQN(), queryString);
					logger.error(message, e);
				} finally {
					User.pop();
				}
			}
		}
		
		subscribedQueryStrings.clear();
		for (User user: userManager.query()) {
			for (String name: user.getBuildQuerySetting().getQuerySubscriptionSupport().getQuerySubscriptions()) {
				fillSubscribedQueryStrings(subscribedQueryStrings, user, 
						NamedQuery.find(settingManager.getBuildSetting().getNamedQueries(), name));
			}
			for (String name: user.getBuildQuerySetting().getQuerySubscriptionSupport().getUserQuerySubscriptions()) { 
				fillSubscribedQueryStrings(subscribedQueryStrings, user, 
						NamedQuery.find(user.getBuildQuerySetting().getUserQueries(), name));
			}
		}

		for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
			User user = entry.getKey();
			for (String queryString: entry.getValue()) {
				User.push(user);
				try {
					if (BuildQuery.parse(null, queryString, true, true).matches(build)) {
						notifyEmails.add(user.getEmail());
						break;
					}
				} catch (Exception e) {
					String message = String.format("Error processing build subscription (user: %s, build: %s, query: %s)", 
							user.getName(), build.getFQN(), queryString);
					logger.error(message, e);
				} finally {
					User.pop();
				}
			}
		}
		
		notify(build, notifyEmails);
	}
	
}
