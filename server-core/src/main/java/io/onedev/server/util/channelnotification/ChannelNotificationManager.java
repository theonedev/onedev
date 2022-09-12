package io.onedev.server.util.channelnotification;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.IOUtils;

import io.onedev.commons.loader.Listen;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentUpdated;
import io.onedev.server.event.issue.IssueChanged;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.pullrequest.PullRequestAssigned;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestChanged;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.event.pullrequest.PullRequestUnassigned;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.ReflectionUtils;

public abstract class ChannelNotificationManager<T extends ChannelNotificationSetting> {

	private static final Logger logger = LoggerFactory.getLogger(ChannelNotificationManager.class);
	
	private final Dao dao;
	
	private final SessionManager sessionManager;
	
	private final ObjectMapper objectMapper;
	
	private final Class<T> settingClass;
	
	@SuppressWarnings("unchecked")
	@Inject
	public ChannelNotificationManager(Dao dao, SessionManager sessionManager, ObjectMapper objectMapper) {
		this.dao = dao;
		this.sessionManager = sessionManager;
		this.objectMapper = objectMapper;
		
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ChannelNotificationManager.class, getClass());
		if (typeArguments.size() == 1 && ChannelNotificationSetting.class.isAssignableFrom(typeArguments.get(0))) {
			settingClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of channel notification manager implementation must "
					+ "be ChannelNotificationManager and must realize the type argument <T>");
		}
	} 
	
	@Transactional
	@Listen
	public void on(IssueEvent event) {
		if (!(event instanceof IssueChanged) 
				|| !(((IssueChanged) event).getChange().getData() instanceof ReferencedFromAware)) {
			sessionManager.runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					IssueEvent clone = (IssueEvent) event.cloneIn(dao);
					
					Issue issue = clone.getIssue();
					User user = clone.getUser();
					
					String issueInfo = String.format("[Issue] (%s - %s)", issue.getFQN(), issue.getTitle()); 
					
					String eventDescription; 
					if (user != null)
						eventDescription = user.getDisplayName() + " " + clone.getActivity();
					else
						eventDescription = StringUtils.capitalize(clone.getActivity());
					
					postIfApplicable(issueInfo + " " + eventDescription, clone);
				}
				
			}, LockUtils.getLock(Issue.getSerialLockName(event.getIssue().getId()), true));
		}
		
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		boolean significantChange = false;
		if (event instanceof PullRequestChanged) {
			PullRequestChangeData changeData = ((PullRequestChanged) event).getChange().getData();
			if (changeData instanceof PullRequestApproveData 
					|| changeData instanceof PullRequestRequestedForChangesData 
					|| changeData instanceof PullRequestMergeData 
					|| changeData instanceof PullRequestDiscardData
					|| changeData instanceof PullRequestReopenData) {
				significantChange = true;
			}
		} else if (!(event instanceof PullRequestMergePreviewCalculated 
				|| event instanceof PullRequestBuildEvent
				|| event instanceof PullRequestReviewRequested
				|| event instanceof PullRequestReviewerRemoved
				|| event instanceof PullRequestAssigned
				|| event instanceof PullRequestUnassigned)) {
			significantChange = true;
		}
		
		if (significantChange) {
			sessionManager.runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					PullRequestEvent clone = (PullRequestEvent) event.cloneIn(dao);
					
					PullRequest request = clone.getRequest();
					User user = clone.getUser();
					
					String pullRequestInfo = String.format("[Pull Request] (%s - %s)", request.getFQN(), request.getTitle()); 
					
					String eventDescription; 
					if (user != null)
						eventDescription = user.getDisplayName() + " " + clone.getActivity();
					else
						eventDescription = StringUtils.capitalize(clone.getActivity());

					postIfApplicable(pullRequestInfo + " " + eventDescription, clone);
				}
				
			}, LockUtils.getLock(PullRequest.getSerialLockName(event.getRequest().getId()), true));
		}
		
	}
	
	@Transactional
	@Listen
	public void on(BuildEvent event) {
		sessionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				BuildEvent clone = (BuildEvent) event.cloneIn(dao);
				
				Build build = clone.getBuild();

				String eventDescription = build.getStatus().toString();
				if (build.getVersion() != null)
					eventDescription = build.getVersion() + " " + eventDescription;
					
				String buildInfo = String.format("[Build] (%s - %s)", build.getFQN(), build.getJobName());
				postIfApplicable(buildInfo + " " + eventDescription, clone);
			}
			
		}, LockUtils.getLock(Build.getSerialLockName(event.getBuild().getId()), true));
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
					if (target == null) 
						target = event.getRefName();
				}
				
				String commitInfo = String.format("[Commit] (%s:%s - %s)", 
						project.getPath(), GitUtils.abbreviateSHA(commit.name()), target);
				postIfApplicable(commitInfo + " " + commit.getShortMessage(), event);
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		if (!(event instanceof CodeCommentUpdated)) {
			sessionManager.runAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					CodeCommentEvent clone = (CodeCommentEvent) event.cloneIn(dao);
					
					CodeComment comment = clone.getComment();

					String commentInfo = String.format("[Code Comment] (%s:%s)", 
							clone.getProject().getPath(), comment.getMark().getPath());
					
					String eventDescription = String.format("%s %s", 
							clone.getUser().getDisplayName(), clone.getActivity());
					
					postIfApplicable(commentInfo + " " + eventDescription, clone);
				}
				
			});			
		}
	}	
	
	private Collection<ChannelNotification> getNotifications(Project project) {
		Map<String, ChannelNotification> notifications = new HashMap<>();
		do {
			T setting = project.getContributedSetting(settingClass);
			for (ChannelNotificationWrapper wrapper: setting.getNotifications()) 
				notifications.putIfAbsent(wrapper.getChannelNotification().getWebhookUrl(), wrapper.getChannelNotification());
			project = project.getParent();
		} while (project != null);
		
		return notifications.values();
	}
	
	private void postIfApplicable(String title, ProjectEvent event) {
		Object data = toJsonObject(title, event);
		
		for (ChannelNotification notification: getNotifications(event.getProject())) {
			if (notification.matches(event)) {
				try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
					HttpPost post = new HttpPost(notification.getWebhookUrl());
					
					StringEntity requestEntity = new StringEntity(objectMapper.writeValueAsString(data), ContentType.APPLICATION_JSON);			
					post.setEntity(requestEntity);
					
					try (CloseableHttpResponse response = client.execute(post)) {
						if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK 
								&& response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
							HttpEntity responseEntity = response.getEntity();
							String errorMessage;
							if (responseEntity != null) {
								String content = IOUtils.readInputStreamToString(responseEntity.getContent());
								errorMessage = String.format("Error sending channel notification (status code: %d, response: %s)", 
										response.getStatusLine().getStatusCode(), content);
							} else {
								errorMessage = String.format("Error sending channel notification (status code: %d)", 
										response.getStatusLine().getStatusCode());
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
	
	protected abstract Object toJsonObject(String title, ProjectEvent event);
	
}
