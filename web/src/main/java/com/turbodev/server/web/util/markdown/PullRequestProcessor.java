package com.turbodev.server.web.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.util.markdown.MarkdownProcessor;
import com.turbodev.server.util.markdown.PullRequestParser;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;

public class PullRequestProcessor extends PullRequestParser implements MarkdownProcessor {
	
	@Override
	public void process(Document document, Object context) {
		parseRequests(document);
	}

	@Override
	protected String toHtml(PullRequest request) {
		CharSequence url = RequestCycle.get().urlFor(
				RequestOverviewPage.class, RequestOverviewPage.paramsOf(request)); 
		return String.format("<a href='%s' class='request'>#%d</a>", url, request.getNumber());
	}
	
}
