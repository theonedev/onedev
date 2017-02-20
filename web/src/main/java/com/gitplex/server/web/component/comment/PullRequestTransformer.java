package com.gitplex.server.web.component.comment;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.markdown.HtmlTransformer;
import com.gitplex.server.util.markdown.PullRequestParser;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;

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
