package com.gitplex.web.component.comment;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;

import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.util.markdown.PullRequestParser;
import com.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.commons.markdown.extensionpoint.HtmlTransformer;

public class PullRequestTransformer extends PullRequestParser implements HtmlTransformer {
	
	@Override
	public Element transform(Element body) {
		parseRequests(body);
		return body;
	}

	@Override
	protected String toHtml(PullRequest request) {
		CharSequence url = RequestCycle.get().urlFor(
				RequestOverviewPage.class, RequestOverviewPage.paramsOf(request)); 
		return String.format("<a href='%s' class='request'>#%d</a>", url, request.getNumber());
	}
	
}
