package io.onedev.server.notification;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.*;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.web.UrlService;
import org.apache.shiro.authz.Permission;
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
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Singleton
public class BuildNotificationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(BuildNotificationManager.class);

	@Inject
	private MailService mailService;

	@Inject
	private UrlService urlService;

	@Inject
	private UserService userService;

	@Inject
	private SettingService settingService;

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
	public void notify(BuildEvent event, Collection<String> emails) {
		emails = emails.stream().filter(it -> !it.equals(User.SYSTEM_EMAIL_ADDRESS)).collect(toList());
		
		if (!emails.isEmpty()) {
			Build build = event.getBuild();

			String status = StringUtils.capitalize(build.getStatus().toString().toLowerCase());
			String subject;
			if (build.getVersion() != null) {
				subject = format(
						"[Build %s] (%s: %s) %s", 
						build.getReference(), 
						build.getJobName(), 
						build.getVersion(), 
						status);
			} else {
				subject = format(
						"[Build %s] (%s) %s", 
						build.getReference(), 
						build.getJobName(), 
						status);
			}

			String summary;
			if (build.getBranch() != null)
				summary = status + " on branch " + build.getBranch();
			else if (build.getTag() != null)
				summary = status + " on tag " + build.getTag();
			else if (build.getRequest() != null)
				summary = status + " on pull request " + build.getRequest().getReference();
			else
				summary = status + " on ref " + build.getRefName();
			
			String url = urlService.urlFor(build, true);
			String threadingReferences = "<" + build.getProject().getPath() + "-build-" + build.getNumber() + "@onedev>";
			String htmlBody = getEmailBody(true, event, summary, null, url, false, null);
			String textBody = getEmailBody(false, event, summary, null, url, false, null);
			mailService.sendMailAsync(Lists.newArrayList(), Lists.newArrayList(), emails, subject, htmlBody,
					textBody, null, null, threadingReferences);
		}
	}
	
	@Sessional
	@Listen
	public void on(BuildEvent event) {
		if (!(event instanceof BuildUpdated) && (event.getUser() == null || !event.getUser().isServiceAccount())) {
			Project project = event.getProject();
			Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
			for (BuildQueryPersonalization personalization: project.getBuildQueryPersonalizations()) {
				var user = personalization.getUser();
				for (String name : personalization.getQuerySubscriptionSupport().getQuerySubscriptions()) {
					String commonName = NamedQuery.getCommonName(name);
					if (commonName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user,
								NamedQuery.find(project.getNamedBuildQueries(), commonName));
					}
					String personalName = NamedQuery.getPersonalName(name);
					if (personalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user,
								NamedQuery.find(personalization.getQueries(), personalName));
					}
				}
			}

			Build build = event.getBuild();
			Collection<String> notifyEmails = new HashSet<>();
			if (event.getUser() != null && event.getUser().isNotifyOwnEvents() 
					&& event.getUser().getPrimaryEmailAddress() != null 
					&& event.getUser().getPrimaryEmailAddress().isVerified()) {
				notifyEmails.add(event.getUser().getPrimaryEmailAddress().getValue());
			}
			for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				Permission permission = new ProjectPermission(build.getProject(), 
						new JobPermission(build.getJobName(), new AccessBuild()));
				if (user.asSubject().isPermitted(permission)) {
					for (String queryString: entry.getValue()) {
						User.push(user);
						try {
							if (BuildQuery.parse(event.getProject(), queryString, true, true).matches(build)) {
								EmailAddress emailAddress = user.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified())
									notifyEmails.add(emailAddress.getValue());
								break;
							}
						} catch (Exception e) {
							String message = String.format("Error processing build subscription (user: %s, build: %s, query: %s)", 
									user.getName(), build.getReference().toString(null), queryString);
							logger.error(message, e);
						} finally {
							User.pop();
						}
					}
				}
			}
			
			subscribedQueryStrings.clear();
			for (User user: userService.query()) {
				for (String name: user.getBuildQueryPersonalization().getQuerySubscriptionSupport().getQuerySubscriptions()) {
					String globalName = NamedQuery.getCommonName(name);
					if (globalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user, 
								NamedQuery.find(settingService.getBuildSetting().getNamedQueries(), globalName));
					}
					String personalName = NamedQuery.getPersonalName(name);
					if (personalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user, 
								NamedQuery.find(user.getBuildQueryPersonalization().getQueries(), personalName));
					}
				}
			}

			for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				Permission permission = new ProjectPermission(build.getProject(), 
						new JobPermission(build.getJobName(), new AccessBuild()));
				if (user.asSubject().isPermitted(permission)) {
					for (String queryString: entry.getValue()) {
						User.push(user);
						try {
							if (BuildQuery.parse(null, queryString, true, true).matches(build)) {
								EmailAddress emailAddress = user.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified())
									notifyEmails.add(emailAddress.getValue());
								break;
							}
						} catch (Exception e) {
							String message = String.format("Error processing build subscription (user: %s, build: %s, query: %s)", 
									user.getName(), build.getReference().toString(null), queryString);
							logger.error(message, e);
						} finally {
							User.pop();
						}
					}
				}
			}
			
			notify(event, notifyEmails);
		}				
	}

}
