package io.onedev.server.plugin.notification.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.markdown.ExternalLinkFormatter;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.notification.ChannelNotificationManager;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.util.commenttext.PlainText;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class DiscordNotificationManager extends ChannelNotificationManager<DiscordNotificationSetting> {
	
	private final ObjectMapper objectMapper;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public DiscordNotificationManager(ObjectMapper objectMapper, MarkdownManager markdownManager) {
		this.objectMapper = objectMapper;
		this.markdownManager = markdownManager;
	}

	@Override
	protected void post(HttpPost post, String title, ProjectEvent event) {
		List<Object> embeds = new ArrayList<>();

		List<String> sections = new ArrayList<>();

		ActivityDetail activityDetail = event.getActivityDetail();
		if (activityDetail != null)
			sections.add(activityDetail.getTextVersion());

		CommentText commentText = event.getCommentText();
		if (commentText instanceof MarkdownText) {
			String markdown = commentText.getPlainContent();

			Set<NodeFormattingHandler<?>> handlers = new HashSet<>();
			handlers.add(new NodeFormattingHandler<>(Link.class, new ExternalLinkFormatter<Link>()));
			handlers.add(new NodeFormattingHandler<>(Image.class, new ExternalLinkFormatter<Image>()));

			sections.add(markdownManager.format(markdown, handlers));
		} else if (commentText instanceof PlainText) {
			sections.add(commentText.getPlainContent());
		}

		sections.add("[Click for more details](" + event.getUrl() + ")");

		embeds.add(CollectionUtils.newHashMap("description", Joiner.on("\n\n").join(sections)));

		try {
			var json = objectMapper.writeValueAsString(CollectionUtils.newHashMap(
					"content", title,
					"embeds", embeds));
			post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
