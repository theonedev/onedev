package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;

import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.gitplex.core.comment.MentionParser;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountHomePage;

public class MentionTransformer extends MentionParser implements HtmlTransformer {
	
	@Override
	public Element transform(Element body) {
		parseMentions(body);
		return body;
	}

	@Override
	protected String toHtml(User user) {
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			CharSequence userUrl = requestCycle.urlFor(AccountHomePage.class, AccountHomePage.paramsOf(user));
			return String.format("<a href='%s' class='mention'>@%s</a>", userUrl, user.getName());
		} else {
			return super.toHtml(user);
		}
	}
	
}
