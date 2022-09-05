package io.onedev.server.plugin.notification.slack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.IOUtils;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.Formatter.Builder;
import com.vladsch.flexmark.formatter.Formatter.FormatterExtension;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.formatter.NodeFormattingHandler.CustomNodeFormatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import io.onedev.commons.loader.Listen;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.entitymanager.SettingManager;
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
import io.onedev.server.mail.MailManager;
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
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.resource.AttachmentResource;

@Singleton
public class SlackNotificationManager {

	private static final Logger logger = LoggerFactory.getLogger(SlackNotificationManager.class);
	
	private final Dao dao;
	
	private final SessionManager sessionManager;
	
	private final ObjectMapper objectMapper;
	
	private final SettingManager settingManager;
	
	private final MailManager mailManager;
	
	@Inject
	public SlackNotificationManager(ObjectMapper objectMapper, Dao dao, SessionManager sessionManager, 
			SettingManager settingManager, MailManager mailManager) {
		this.objectMapper = objectMapper;
		this.dao = dao;
		this.sessionManager = sessionManager;
		this.settingManager = settingManager;
		this.mailManager = mailManager;
	} 
	
	private Collection<ChannelNotification> getNotifications(Project project) {
		Map<String, ChannelNotification> notifications = new HashMap<>();
		do {
			SlackNotificationSetting setting = project.getContributedSetting(SlackNotificationSetting.class);
			for (ChannelNotificationWrapper wrapper: setting.getNotifications()) 
				notifications.putIfAbsent(wrapper.getChannelNotification().getWebhookUrl(), wrapper.getChannelNotification());
			project = project.getParent();
		} while (project != null);
		
		return notifications.values();
	}
	
	private String toMrkdwn(String markdown) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(markdown);

		Collection<FormatterExtension> extensions = new ArrayList<>();
		extensions.add(new FormatterExtension() {

			@Override
			public void rendererOptions(MutableDataHolder options) {
			}

			@Override
			public void extend(Builder formatterBuilder) {
				formatterBuilder.nodeFormatterFactory(new NodeFormatterFactory() {

					@Override
					public @NotNull NodeFormatter create(@NotNull DataHolder options) {
						return new NodeFormatter() {

							private void renderInlineLink(@NotNull InlineLinkNode node, @NotNull NodeFormatterContext context,
									@NotNull MarkdownWriter markdown) {
								markdown.append("<");
								String url = node.getUrl().toString();
								if (url.startsWith("/")) {
									url = settingManager.getSystemSetting().getServerUrl() + url;
									markdown.append(AttachmentResource.authorizeGroup(url));
								} else {
									markdown.append(url);
								}
								markdown.append("|");
								context.renderChildren(node);
								markdown.append(">");
							}
							
							@Override
							public @Nullable Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
								Set<NodeFormattingHandler<?>> handlers = new HashSet<>();
								handlers.add(new NodeFormattingHandler<>(Emphasis.class, new CustomNodeFormatter<Emphasis>() {

									@Override
									public void render(@NotNull Emphasis node, @NotNull NodeFormatterContext context,
											@NotNull MarkdownWriter markdown) {
								        markdown.append("_");
								        context.renderChildren(node);
								        markdown.append("_");
									}
									
								}));
								handlers.add(new NodeFormattingHandler<>(StrongEmphasis.class, new CustomNodeFormatter<StrongEmphasis>() {

									@Override
									public void render(@NotNull StrongEmphasis node, @NotNull NodeFormatterContext context,
											@NotNull MarkdownWriter markdown) {
								        markdown.append("*");
								        context.renderChildren(node);
								        markdown.append("*");
									}
									
								}));
								handlers.add(new NodeFormattingHandler<>(Link.class, new CustomNodeFormatter<Link>() {

									@Override
									public void render(@NotNull Link node, @NotNull NodeFormatterContext context,
											@NotNull MarkdownWriter markdown) {
										renderInlineLink(node, context, markdown);
									}
									
								}));
								handlers.add(new NodeFormattingHandler<>(Image.class, new CustomNodeFormatter<Image>() {

									@Override
									public void render(@NotNull Image node, @NotNull NodeFormatterContext context,
											@NotNull MarkdownWriter markdown) {
										renderInlineLink(node, context, markdown);
									}
									
								}));
								return handlers;
							}

							@Override
							public @Nullable Set<Class<?>> getNodeClasses() {
								return null;
							}
							
						};
					}
					
				});
			}
			
		});
		return Formatter.builder().extensions(extensions).build().render(node);
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
					
					post(clone, issueInfo + " " + eventDescription, null);
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

					post(clone, pullRequestInfo + " " + eventDescription, null);
				}
				
			}, LockUtils.getLock(PullRequest.getSerialLockName(event.getRequest().getId()), true));
		}
		
	}
	
	private void post(ProjectEvent event, String title, @Nullable String body) {
		List<Object> blocks = new ArrayList<>();
		
		blocks.add(CollectionUtils.newHashMap(
				"type", "section", 
				"text", CollectionUtils.newHashMap(
						"type", "plain_text", 
						"text", title)));
		
		if (body != null) {
			blocks.add(CollectionUtils.newHashMap(
					"type", "section", 
					"text", CollectionUtils.newHashMap(
							"type", "plain_text", 
							"text", body)));			
		}
		
		ActivityDetail activityDetail = event.getActivityDetail();
		if (activityDetail != null) {
			blocks.add(CollectionUtils.newHashMap(
					"type", "section", 
					"text", CollectionUtils.newHashMap(
							"type", "plain_text", 
							"text", activityDetail.getTextVersion())));			
		}
		
		String markdown = event.getMarkdown();
		if (markdown != null) {
			if (mailManager.isMailContent(markdown)) {
				blocks.add(CollectionUtils.newHashMap(
						"type", "section", 
						"text", CollectionUtils.newHashMap(
								"type", "plain_text", 
								"text", mailManager.toPlainText(markdown))));			
			} else {
				blocks.add(CollectionUtils.newHashMap(
						"type", "section", 
						"text", CollectionUtils.newHashMap(
								"type", "mrkdwn", 
								"text", toMrkdwn(markdown))));			
			}
		}
		
		blocks.add(CollectionUtils.newHashMap(
				"type", "section", 
				"text", CollectionUtils.newHashMap(
						"type", "mrkdwn", 
						"text", "<" + event.getUrl() + "|Click here for details>")));			
		
		Map<Object, Object> data = CollectionUtils.newHashMap(
				"text", title, 
				"blocks", blocks);
		
		for (ChannelNotification notification: getNotifications(event.getProject())) {
			if (notification.matches(event)) {
				try (CloseableHttpClient client = HttpClients.createDefault()) {
					HttpPost post = new HttpPost(notification.getWebhookUrl());
					
					StringEntity entity = new StringEntity(objectMapper.writeValueAsString(data), ContentType.APPLICATION_JSON);			
					post.setEntity(entity);
					
					try (CloseableHttpResponse response = client.execute(post)) {
						if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
							String content = IOUtils.readInputStreamToString(response.getEntity().getContent());
							String errorMessage = String.format("Error sending slack notification (status code: %d, response: %s)", 
									response.getStatusLine().getStatusCode(), content);
							logger.error(errorMessage);
						}
					}
				} catch (Exception e) {
					logger.error("Error sending slack notification", e);
				}
			}
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
				post(clone, buildInfo + " " + eventDescription, null);
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
				post(event, commitInfo + " " + commit.getShortMessage(), GitUtils.getDetailMessage(commit));
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
					
					post(clone, commentInfo + " " + eventDescription, null);
				}
				
			});			
		}
	}	
	
}
