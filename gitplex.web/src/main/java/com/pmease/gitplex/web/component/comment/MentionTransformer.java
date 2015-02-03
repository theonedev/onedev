package com.pmease.gitplex.web.component.comment;

import org.jsoup.nodes.Element;

import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.MentionParser;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.model.User;

public class MentionTransformer extends MentionParser implements HtmlTransformer {
	
	@Override
	public Element transform(Element body) {
		parseMentions(body);
		return body;
	}

	@Override
	protected String toHtml(User user) {
		return String.format("<a href='%s' class='mention'>@%s</a>", 
				GitPlex.getInstance(UrlManager.class).urlFor(user), user.getName());
	}
	
}
