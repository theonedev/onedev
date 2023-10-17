package io.onedev.server.notification;

import com.nimbusds.jose.util.IOUtils;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.project.codecomment.CodeCommentEdited;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.*;
import io.onedev.server.model.support.channelnotification.ChannelNotification;
import io.onedev.server.model.support.channelnotification.ChannelNotificationSetting;
import io.onedev.server.model.support.channelnotification.ChannelNotificationWrapper;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ChannelNotificationManager<T extends ChannelNotificationSetting> {

	private static final Logger logger = LoggerFactory.getLogger(ChannelNotificationManager.class);

	private final Class<T> settingClass;

	public ChannelNotificationManager() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ChannelNotificationManager.class, getClass());
		if (typeArguments.size() == 1 && ChannelNotificationSetting.class.isAssignableFrom(typeArguments.get(0))) {
			settingClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of channel notification manager implementation must " + "be ChannelNotificationManager and must realize the type argument <T>");
		}
	}

	@Sessional
	@Listen
	public void on(IssueEvent event) {
		if (!event.isMinor()) {
			Issue issue = event.getIssue();
			User user = event.getUser();

			String issueInfo = String.format("[Issue] (%s - %s)", issue.getFQN(), issue.getTitle());

			String eventDescription;
			if (user != null) 
				eventDescription = user.getDisplayName() + " " + event.getActivity();
			else 
				eventDescription = StringUtils.capitalize(event.getActivity());

			postIfApplicable(issueInfo + " " + eventDescription, event);
		}
	}

	@Sessional
	@Listen
	public void on(PullRequestEvent event) {
		if (!event.isMinor()) {
			PullRequest request = event.getRequest();
			User user = event.getUser();

			String pullRequestInfo = String.format("[Pull Request] (%s - %s)", request.getFQN(), request.getTitle());

			String eventDescription;
			if (user != null) eventDescription = user.getDisplayName() + " " + event.getActivity();
			else eventDescription = StringUtils.capitalize(event.getActivity());

			postIfApplicable(pullRequestInfo + " " + eventDescription, event);
		}

	}

	@Sessional
	@Listen
	public void on(BuildEvent event) {
		Build build = event.getBuild();

		String eventDescription = build.getStatus().toString();
		if (build.getVersion() != null) eventDescription = build.getVersion() + " " + eventDescription;

		String buildInfo = String.format("[Build] (%s - %s)", build.getFQN(), build.getJobName());
		postIfApplicable(buildInfo + " " + eventDescription, event);
	}

	@Sessional
	@Listen
	public void on(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId())) {
			Project project = event.getProject();
			RevCommit commit = project.getRevCommit(event.getNewCommitId(), false);
			if (commit != null) {
				String target = GitUtils.ref2branch(event.getRefName());
				if (target == null) {
					target = GitUtils.ref2tag(event.getRefName());
					if (target == null) target = event.getRefName();
				}

				String commitInfo = String.format("[Commit] (%s:%s - %s)", project.getPath(), GitUtils.abbreviateSHA(commit.name()), target);
				postIfApplicable(commitInfo + " " + commit.getShortMessage(), event);
			}
		}
	}

	@Sessional
	@Listen
	public void on(CodeCommentEvent event) {
		if (!(event instanceof CodeCommentEdited)) {
			CodeComment comment = event.getComment();

			String commentInfo = String.format("[Code Comment] (%s:%s)", event.getProject().getPath(), comment.getMark().getPath());

			String eventDescription = String.format("%s %s", event.getUser().getDisplayName(), event.getActivity());

			postIfApplicable(commentInfo + " " + eventDescription, event);
		}
	}

	private Collection<ChannelNotification> getNotifications(Project project) {
		Map<String, ChannelNotification> notifications = new HashMap<>();
		do {
			T setting = project.getContributedSetting(settingClass);
			for (ChannelNotificationWrapper wrapper : setting.getNotifications())
				notifications.putIfAbsent(wrapper.getChannelNotification().getWebhookUrl(), wrapper.getChannelNotification());
			project = project.getParent();
		} while (project != null);

		return notifications.values();
	}

	private void postIfApplicable(String title, ProjectEvent event) {
		for (ChannelNotification notification : getNotifications(event.getProject())) {
			if (notification.matches(event)) {
				try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
					HttpPost post = new HttpPost(notification.getWebhookUrl());
					post(post, title, event);
					try (CloseableHttpResponse response = client.execute(post)) {
						if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK && response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
							HttpEntity responseEntity = response.getEntity();
							String errorMessage;
							if (responseEntity != null) {
								String content = IOUtils.readInputStreamToString(responseEntity.getContent());
								errorMessage = String.format("Error sending channel notification (status code: %d, response: %s)", response.getStatusLine().getStatusCode(), content);
							} else {
								errorMessage = String.format("Error sending channel notification (status code: %d)", response.getStatusLine().getStatusCode());
							}
							logger.error(errorMessage);
						}
					}
				} catch (Exception e) {
					logger.error("Error sending channel notification", e);
				}
			}
		}
	}
	
	protected abstract void post(HttpPost post, String title, ProjectEvent event);

}
