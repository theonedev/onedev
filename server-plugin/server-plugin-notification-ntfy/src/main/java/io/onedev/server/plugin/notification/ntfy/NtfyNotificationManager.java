package io.onedev.server.plugin.notification.ntfy;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.markdown.ExternalLinkFormatter;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.notification.ChannelNotificationManager;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;
import io.onedev.server.util.commenttext.PlainText;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.*;
import static org.apache.http.entity.ContentType.create;

@Singleton
public class NtfyNotificationManager extends ChannelNotificationManager<NtfyNotificationSetting> {
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public NtfyNotificationManager(MarkdownManager markdownManager) {
		this.markdownManager = markdownManager;
	}

	@Override
	protected void post(HttpPost post, String title, ProjectEvent event) {
		post.setHeader("Markdown", "yes");
		post.setHeader("Title", title);
		post.setHeader("Click", event.getUrl());

		var body = new StringBuilder();

		ActivityDetail activityDetail = event.getActivityDetail();
		if (activityDetail != null) 
			body.append(activityDetail.getTextVersion()).append("\n\n");

		CommentText commentText = event.getCommentText();
		if (commentText instanceof MarkdownText) {
			String markdown = commentText.getPlainContent();

			Set<NodeFormattingHandler<?>> handlers = new HashSet<>();
			handlers.add(new NodeFormattingHandler<>(Link.class, new ExternalLinkFormatter<>()));
			handlers.add(new NodeFormattingHandler<>(Image.class, new ExternalLinkFormatter<>()));

			body.append(markdownManager.format(markdown, handlers));
		} else if (commentText instanceof PlainText) {
			body.append(commentText.getPlainContent());
		}
		
		post.setEntity(new StringEntity(body.toString(), create("text/markdown", UTF_8)));
	}
}
