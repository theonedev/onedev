package io.onedev.server.notification;

import com.google.common.collect.Lists;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.pack.PackEvent;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.*;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadPack;
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
import static java.util.stream.Collectors.toList;

@Singleton
public class PackNotificationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(PackNotificationManager.class);

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
	public void notify(PackEvent event, Collection<String> emails) {
		emails = emails.stream().filter(it -> !it.equals(User.SYSTEM_EMAIL_ADDRESS)).collect(toList());
		
		if (!emails.isEmpty()) {
			Pack pack = event.getPack();
			String subject = String.format("[%s %s] Package published", pack.getType(), pack.getReference(true));

			String summary;
			if (pack.getUser() != null)
				summary = "Package published by user " + pack.getUser().getDisplayName();
			else
				summary = "Package published via build " + pack.getBuild().getReference();

			String url = urlService.urlFor(pack, true);
			String threadingReferences = "<" + pack.getProject().getPath() + "-pack-" + pack.getId() + "@onedev>";
			String htmlBody = getEmailBody(true, event, summary, null, url, false, null);
			String textBody = getEmailBody(false, event, summary, null, url, false, null);
			mailService.sendMailAsync(Lists.newArrayList(), Lists.newArrayList(), emails, subject, htmlBody,
					textBody, null, null, threadingReferences);
		}
	}
	
	@Sessional
	@Listen
	public void on(PackEvent event) {
		if (!event.getUser().isServiceAccount()) {
			Project project = event.getProject();
			Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
			for (PackQueryPersonalization personalization: project.getPackQueryPersonalizations()) {
				for (String name: personalization.getQuerySubscriptionSupport().getQuerySubscriptions()) {
					String commonName = NamedQuery.getCommonName(name);
					if (commonName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, personalization.getUser(), 
								NamedQuery.find(project.getNamedPackQueries(), commonName));
					}
					String personalName = NamedQuery.getPersonalName(name);
					if (personalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, personalization.getUser(), 
								NamedQuery.find(personalization.getQueries(), personalName));
					}
				}
			}
	
			Pack pack = event.getPack();
			Collection<String> notifyEmails = new HashSet<>();
			for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				Permission permission = new ProjectPermission(pack.getProject(), new ReadPack());
				if (user.asSubject().isPermitted(permission)) {
					for (String queryString: entry.getValue()) {
						User.push(user);
						try {
							if (PackQuery.parse(event.getProject(), queryString, true).matches(pack)) {
								EmailAddress emailAddress = user.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified())
									notifyEmails.add(emailAddress.getValue());
								break;
							}
						} catch (Exception e) {
							String message = String.format("Error processing package subscription (user: %s, project: %s, package: %s, query: %s)", 
									user.getName(), pack.getProject().getPath(), pack.getType() + " " + pack.getReference(false), queryString);
							logger.error(message, e);
						} finally {
							User.pop();
						}
					}
				}
			}
			
			subscribedQueryStrings.clear();
			for (User user: userService.query()) {
				for (String name: user.getPackQueryPersonalization().getQuerySubscriptionSupport().getQuerySubscriptions()) {
					String globalName = NamedQuery.getCommonName(name);
					if (globalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user, 
								NamedQuery.find(settingService.getPackSetting().getNamedQueries(), globalName));
					}
					String personalName = NamedQuery.getPersonalName(name);
					if (personalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user, 
								NamedQuery.find(user.getPackQueryPersonalization().getQueries(), personalName));
					}
				}
			}
	
			for (Map.Entry<User, Collection<String>> entry: subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				Permission permission = new ProjectPermission(pack.getProject(), new ReadPack());
				if (user.asSubject().isPermitted(permission)) {
					for (String queryString: entry.getValue()) {
						User.push(user);
						try {
							if (PackQuery.parse(null, queryString, true).matches(pack)) {
								EmailAddress emailAddress = user.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified())
									notifyEmails.add(emailAddress.getValue());
								break;
							}
						} catch (Exception e) {
							String message = String.format("Error processing package subscription (user: %s, project: %s, package: %s, query: %s)", 
									user.getName(), pack.getProject().getPath(), pack.getType() + " " + pack.getReference(false), queryString);
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
