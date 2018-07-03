package io.onedev.server.web.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.markdown.MarkdownProcessor;
import io.onedev.server.util.markdown.PullRequestParser;
import io.onedev.server.web.page.project.pullrequests.requestdetail.activities.RequestActivitiesPage;

public class PullRequestProcessor extends PullRequestParser implements MarkdownProcessor {
	
	@Override
	public void process(Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected String toHtml(PullRequest request) {
		CharSequence url = RequestCycle.get().urlFor(
				RequestActivitiesPage.class, RequestActivitiesPage.paramsOf(request)); 
		return String.format("<a href='%s' class='request'>#%d</a>", url, request.getNumber());
	}
	
}
