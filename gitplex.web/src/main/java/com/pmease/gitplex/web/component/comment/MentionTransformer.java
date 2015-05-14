package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;

import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.gitplex.core.comment.MentionParser;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

public class MentionTransformer extends MentionParser implements HtmlTransformer {
	
	@Override
	public Element transform(Element body) {
		parseMentions(body);
		return body;
	}

	@Override
	protected String toHtml(User user) {
		if (RequestCycle.get() != null) {
			return String.format("<a href='%s' class='mention'>@%s</a>", 
				RequestCycle.get().urlFor(AccountOverviewPage.class, AccountPage.paramsOf(user)), user.getName());
		} else {
			return super.toHtml(user);
		}
	}
	
}
