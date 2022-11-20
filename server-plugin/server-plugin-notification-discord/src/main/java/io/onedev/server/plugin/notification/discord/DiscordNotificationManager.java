package io.onedev.server.plugin.notification.discord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import io.onedev.server.util.channelnotification.ChannelNotificationManager;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.util.commenttext.PlainText;

@Singleton
public class DiscordNotificationManager extends ChannelNotificationManager<DiscordNotificationSetting> {

	private final MarkdownManager markdownManager;
	
	@Inject
	public DiscordNotificationManager(ObjectMapper objectMapper, MarkdownManager markdownManager) {
		super(objectMapper);
		this.markdownManager = markdownManager;
	}

	@Override
	protected Object toJsonObject(String title, ProjectEvent event) {
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
		
		return CollectionUtils.newHashMap(
				"content", title, 
				"embeds", embeds);
	}

}
