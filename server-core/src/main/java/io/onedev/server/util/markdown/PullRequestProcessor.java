package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class PullRequestProcessor extends PullRequestParser implements MarkdownProcessor {
	
	@Override
	public void process(@Nullable Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected PullRequest findReferenceable(Project project, long number) {
		if (SecurityUtils.canReadCode(project))
			return super.findReferenceable(project, number);
		else
			return null;
	}

	@Override
	protected String toHtml(PullRequest request, String text) {
		CharSequence url = RequestCycle.get().urlFor(
				PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(request, null)); 
		return String.format("<a href='%s' class='pull-request reference' data-reference=%d>%s</a>", url, request.getId(), text);
	}
	
}
