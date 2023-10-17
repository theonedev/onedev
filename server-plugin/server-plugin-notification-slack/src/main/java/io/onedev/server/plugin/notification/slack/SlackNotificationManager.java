package io.onedev.server.plugin.notification.slack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.formatter.NodeFormattingHandler.CustomNodeFormatter;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.notification.ChannelNotificationManager;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.util.commenttext.PlainText;

@Singleton
public class SlackNotificationManager extends ChannelNotificationManager<SlackNotificationSetting> {

	private final ObjectMapper objectMapper;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public SlackNotificationManager(ObjectMapper objectMapper, MarkdownManager markdownManager) {
		this.objectMapper = objectMapper;
		this.markdownManager = markdownManager;
	} 
	
	private void renderInlineLink(@NotNull InlineLinkNode node, @NotNull NodeFormatterContext context,
			@NotNull MarkdownWriter markdown) {
		markdown.append("<");
		markdown.append(markdownManager.toExternalUrl(node.getUrl().toString()));
		markdown.append("|");
		context.renderChildren(node);
		markdown.append(">");
	}

	@Override
	protected void post(HttpPost post, String title, ProjectEvent event) {
		List<Object> blocks = new ArrayList<>();

		blocks.add(CollectionUtils.newHashMap(
				"type", "section",
				"text", CollectionUtils.newHashMap(
						"type", "plain_text",
						"text", title)));

		ActivityDetail activityDetail = event.getActivityDetail();
		if (activityDetail != null) {
			blocks.add(CollectionUtils.newHashMap(
					"type", "section",
					"text", CollectionUtils.newHashMap(
							"type", "plain_text",
							"text", activityDetail.getTextVersion())));
		}

		CommentText commentText = event.getCommentText();
		if (commentText instanceof MarkdownText) {
			String markdown = commentText.getPlainContent();

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

			blocks.add(CollectionUtils.newHashMap(
					"type", "section",
					"text", CollectionUtils.newHashMap(
							"type", "mrkdwn",
							"text", markdownManager.format(markdown, handlers))));
		} else if (commentText instanceof PlainText) {
			blocks.add(CollectionUtils.newHashMap(
					"type", "section",
					"text", CollectionUtils.newHashMap(
							"type", "plain_text",
							"text", commentText.getPlainContent())));
		}

		blocks.add(CollectionUtils.newHashMap(
				"type", "section",
				"text", CollectionUtils.newHashMap(
						"type", "mrkdwn",
						"text", "<" + event.getUrl() + "|Click here for details>")));

		try {
			var json = objectMapper.writeValueAsString(CollectionUtils.newHashMap(
					"text", title,
					"blocks", blocks));
			post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
