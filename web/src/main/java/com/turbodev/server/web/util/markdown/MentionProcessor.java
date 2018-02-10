package com.turbodev.server.web.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import com.turbodev.server.model.User;
import com.turbodev.server.util.markdown.MarkdownProcessor;
import com.turbodev.server.util.markdown.MentionParser;
import com.turbodev.server.web.page.user.UserProfilePage;

public class MentionProcessor extends MentionParser implements MarkdownProcessor {
	
	@Override
	public void process(Document document, Object context) {
		parseMentions(document);
	}

	@Override
	protected String toHtml(User user) {
		if (RequestCycle.get() != null) {
			return String.format("<a href='%s' class='mention'>@%s</a>", 
				RequestCycle.get().urlFor(UserProfilePage.class, UserProfilePage.paramsOf(user)), user.getName());
		} else {
			return super.toHtml(user);
		}
	}
	
}
