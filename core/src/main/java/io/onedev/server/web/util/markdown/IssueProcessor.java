package io.onedev.server.web.util.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.markdown.IssueParser;
import io.onedev.server.util.markdown.MarkdownProcessor;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;

public class IssueProcessor extends IssueParser implements MarkdownProcessor {
	
	@Override
	public void process(Project project, Document document, Object context) {
		parseReferences(project, document);
	}

	@Override
	protected String toHtml(Issue issue) {
		CharSequence url = RequestCycle.get().urlFor(
				IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null)); 
		return String.format("<a href='%s' class='issue'>#%d</a>", url, issue.getNumber());
	}
	
}
