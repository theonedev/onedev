package io.onedev.server.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;

public class MentionProcessor extends MentionParser implements MarkdownProcessor {
	
	@Override
	public void process(Document document, Project project, Object context) {
		parseMentions(document);
	}

	@Override
	protected String toHtml(String userName) {
		if (RequestCycle.get() != null && OneDev.getInstance(UserManager.class).findByName(userName) != null) {
			return String.format("<a class='reference mention' data-reference='%s'>@%s</a>", 
					userName, userName);
		} else {
			return super.toHtml(userName);
		}
	}
	
}
