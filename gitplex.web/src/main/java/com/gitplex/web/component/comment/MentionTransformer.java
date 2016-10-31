package com.gitplex.web.component.comment;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;

import com.gitplex.core.entity.Account;
import com.gitplex.core.util.markdown.MentionParser;
import com.gitplex.web.page.account.overview.AccountOverviewPage;
import com.gitplex.commons.markdown.extensionpoint.HtmlTransformer;

public class MentionTransformer extends MentionParser implements HtmlTransformer {
	
	@Override
	public Element transform(Element body) {
		parseMentions(body);
		return body;
	}

	@Override
	protected String toHtml(Account user) {
		if (RequestCycle.get() != null) {
			return String.format("<a href='%s' class='mention'>@%s</a>", 
				RequestCycle.get().urlFor(AccountOverviewPage.class, AccountOverviewPage.paramsOf(user)), user.getName());
		} else {
			return super.toHtml(user);
		}
	}
	
}
