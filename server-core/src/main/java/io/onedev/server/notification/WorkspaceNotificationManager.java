package io.onedev.server.notification;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.workspace.WorkspaceEvent;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.WorkspaceQueryPersonalization;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;
import io.onedev.server.service.UserService;

@Singleton
public class WorkspaceNotificationManager {

	private static final Logger logger = LoggerFactory.getLogger(WorkspaceNotificationManager.class);

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
	public void notify(WorkspaceEvent event, Collection<String> emails) {
		emails = emails.stream().filter(it -> !it.equals(User.SYSTEM_EMAIL_ADDRESS)).collect(toList());

		if (!emails.isEmpty()) {
			Workspace workspace = event.getWorkspace();
			String subject = String.format("[Workspace #%d] %s",
					workspace.getNumber(),
					event.getActivity());

			String summary = String.format("Dev session %s by user %s on branch %s",
					event.getActivity(),
					workspace.getUser().getDisplayName(),
					workspace.getBranch());

			String url = urlService.urlFor(workspace, true);
			String threadingReferences = "<" + workspace.getProject().getPath()
					+ "-workspace-" + workspace.getNumber() + "@onedev>";
			String htmlBody = getEmailBody(true, event, summary, null, url, false, null);
			String textBody = getEmailBody(false, event, summary, null, url, false, null);
			mailService.sendMailAsync(Lists.newArrayList(), Lists.newArrayList(), emails, subject, htmlBody,
					textBody, null, null, threadingReferences);
		}
	}

	@Sessional
	@Listen
	public void on(WorkspaceEvent event) {
		if (event.getUser() == null || event.getUser().getType() != User.Type.SERVICE) {
			Project project = event.getProject();
			Map<User, Collection<String>> subscribedQueryStrings = new HashMap<>();
			for (WorkspaceQueryPersonalization personalization : project.getWorkspaceQueryPersonalizations()) {
				for (String name : personalization.getQuerySubscriptionSupport().getQuerySubscriptions()) {
					String commonName = NamedQuery.getCommonName(name);
					if (commonName != null) {
						// Skip common query subscription if a personal query with same name exists
						if (NamedQuery.find(personalization.getQueries(), commonName) == null) {
							fillSubscribedQueryStrings(subscribedQueryStrings, personalization.getUser(),
									NamedQuery.find(project.getNamedWorkspaceQueries(), commonName));
						}
					}
					String personalName = NamedQuery.getPersonalName(name);
					if (personalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, personalization.getUser(),
								NamedQuery.find(personalization.getQueries(), personalName));
					}
				}
			}
	
			Workspace workspace = event.getWorkspace();
			Collection<String> notifyEmails = new HashSet<>();
			for (Map.Entry<User, Collection<String>> entry : subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				Permission permission = new ProjectPermission(workspace.getProject(), new WriteCode());
				if (user.asSubject().isPermitted(permission)) {
					for (String queryString : entry.getValue()) {
						User.push(user);
						try {
							if (WorkspaceQuery.parse(event.getProject(), queryString, true).matches(workspace)) {
								EmailAddress emailAddress = user.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified())
									notifyEmails.add(emailAddress.getValue());
								break;
							}
						} catch (Exception e) {
							String message = String.format(
									"Error processing workspace subscription (user: %s, project: %s, workspace: #%d, query: %s)",
									user.getName(), workspace.getProject().getPath(), workspace.getNumber(), queryString);
							logger.error(message, e);
						} finally {
							User.pop();
						}
					}
				}
			}
	
			subscribedQueryStrings.clear();
			for (User user : userService.query()) {
				for (String name : user.getWorkspaceQueryPersonalization().getQuerySubscriptionSupport().getQuerySubscriptions()) {
					String globalName = NamedQuery.getCommonName(name);
					if (globalName != null) {
						// Skip common query subscription if a personal query with same name exists
						if (NamedQuery.find(user.getWorkspaceQueryPersonalization().getQueries(), globalName) == null) {
							fillSubscribedQueryStrings(subscribedQueryStrings, user,
									NamedQuery.find(settingService.getWorkspaceSetting().getNamedQueries(), globalName));
						}
					}
					String personalName = NamedQuery.getPersonalName(name);
					if (personalName != null) {
						fillSubscribedQueryStrings(subscribedQueryStrings, user,
								NamedQuery.find(user.getWorkspaceQueryPersonalization().getQueries(), personalName));
					}
				}
			}
	
			for (Map.Entry<User, Collection<String>> entry : subscribedQueryStrings.entrySet()) {
				User user = entry.getKey();
				Permission permission = new ProjectPermission(workspace.getProject(), new WriteCode());
				if (user.asSubject().isPermitted(permission)) {
					for (String queryString : entry.getValue()) {
						User.push(user);
						try {
							if (WorkspaceQuery.parse(null, queryString, true).matches(workspace)) {
								EmailAddress emailAddress = user.getPrimaryEmailAddress();
								if (emailAddress != null && emailAddress.isVerified())
									notifyEmails.add(emailAddress.getValue());
								break;
							}
						} catch (Exception e) {
							String message = String.format(
									"Error processing workspace subscription (user: %s, project: %s, workspace: #%d, query: %s)",
									user.getName(), workspace.getProject().getPath(), workspace.getNumber(), queryString);
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
