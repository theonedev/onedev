package com.gitplex.server.web.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import com.gitplex.server.model.User;
import com.gitplex.server.util.markdown.MarkdownProcessor;
import com.gitplex.server.util.markdown.MentionParser;
import com.gitplex.server.web.page.user.UserProfilePage;

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
