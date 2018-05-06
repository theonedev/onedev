package io.onedev.server.web.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.markdown.MarkdownProcessor;
import io.onedev.server.util.markdown.MentionParser;
import io.onedev.server.web.page.user.UserProfilePage;

public class MentionProcessor extends MentionParser implements MarkdownProcessor {
	
	@Override
	public void process(Project project, Document document, Object context) {
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
