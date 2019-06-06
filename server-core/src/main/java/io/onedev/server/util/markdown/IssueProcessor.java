package io.onedev.server.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;

public class IssueProcessor extends IssueParser implements MarkdownProcessor {
	
	@Override
	public void process(Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected String toHtml(Issue issue, String text) {
		CharSequence url = RequestCycle.get().urlFor(
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null)); 
		return String.format("<a href='%s' class='issue reference' data-reference=%d>%s</a>", url, issue.getId(), text);
	}
	
}
