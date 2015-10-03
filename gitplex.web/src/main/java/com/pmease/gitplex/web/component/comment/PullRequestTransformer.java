package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Element;

import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.gitplex.core.markdown.PullRequestParser;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;

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
		return String.format("<a href='%s' class='request'>#%d</a>", url, request.getId());
	}
	
}
